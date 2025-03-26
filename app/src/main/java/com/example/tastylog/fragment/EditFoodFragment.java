package com.example.tastylog.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.tastylog.AppwriteWrapper;
import com.example.tastylog.MainActivity;
import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 编辑食物Fragment
 * 
 * 允许用户编辑已有的食物记录
 */
public class EditFoodFragment extends BaseFragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final String ARG_FOOD_ITEM = "food_item";

    private TextInputEditText etStoreName;
    private TextInputEditText etPrice;
    private TextInputEditText etNotes;
    private TextInputEditText etLocation;
    private TextInputEditText etDate;
    private RatingBar ratingBar;
    private ImageView ivFoodPhoto;
    private ChipGroup chipGroup;
    private Uri photoUri;
    private FrameLayout photoContainer;
    private Chip chipAdd;
    private Calendar selectedDate = Calendar.getInstance();
    private FoodItem foodItemToEdit;
    private boolean imageChanged = false;

    /**
     * 创建EditFoodFragment实例
     * 
     * @param foodItem 要编辑的食物记录
     * @return EditFoodFragment实例
     */
    public static EditFoodFragment newInstance(FoodItem foodItem) {
        EditFoodFragment fragment = new EditFoodFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FOOD_ITEM, foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            foodItemToEdit = (FoodItem) getArguments().getSerializable(ARG_FOOD_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用编辑页面的布局文件
        View view = inflater.inflate(R.layout.fragment_edit_food, container, false);
        
        // 初始化视图和设置初始数据
        initViews(view);
        populateViewsWithFoodData();
        
        return view;
    }

    private void initViews(View view) {
        // 初始化视图
        etStoreName = view.findViewById(R.id.et_store_name);
        etPrice = view.findViewById(R.id.et_price);
        etNotes = view.findViewById(R.id.et_notes);
        etLocation = view.findViewById(R.id.et_location);
        etDate = view.findViewById(R.id.et_date);
        ratingBar = view.findViewById(R.id.rating_bar);
        chipGroup = view.findViewById(R.id.chip_group);
        photoContainer = view.findViewById(R.id.layout_photo);
        chipAdd = view.findViewById(R.id.chip_add);
        
        // 设置标题
        TextView tvTitle = view.findViewById(R.id.toolbar_title);
        tvTitle.setText("编辑美食");
        
        // 确保EditText支持多语言输入
        etStoreName.setInputType(InputType.TYPE_CLASS_TEXT);
        etNotes.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        
        // 设置返回按钮
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        
        // 设置保存按钮
        Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setText("更新"); // 修改按钮文本
        btnSave.setOnClickListener(v -> {
            updateFoodItem(); // 调用更新方法
        });
        
        // 设置照片容器点击事件
        photoContainer.setOnClickListener(v -> {
            showPhotoOptions();
        });
        
        // 设置添加标签点击事件
        chipAdd.setOnClickListener(v -> {
            showAddTagDialog();
        });
        
        // 设置日期选择框点击事件
        etDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });
    }

    // 用现有食物数据填充视图
    private void populateViewsWithFoodData() {
        if (foodItemToEdit != null) {
            etStoreName.setText(foodItemToEdit.getTitle());
            
            // 处理价格 - 移除前缀"¥"
            String priceStr = foodItemToEdit.getPrice();
            if (priceStr != null && priceStr.startsWith("¥")) {
                priceStr = priceStr.substring(1);
            }
            etPrice.setText(priceStr);
            
            etDate.setText(foodItemToEdit.getTime());
            etNotes.setText(foodItemToEdit.getContent());
            etLocation.setText(foodItemToEdit.getLocation());
            ratingBar.setRating(foodItemToEdit.getRating());
            
            // 处理标签
            setupTags(foodItemToEdit.getTags());
            
            // 处理图片
            String imageUrl = foodItemToEdit.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                loadImage(imageUrl);
            }
            
            // 初始化日期对象
            if (foodItemToEdit.getTime() != null && !foodItemToEdit.getTime().isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = format.parse(foodItemToEdit.getTime());
                    if (date != null) {
                        selectedDate.setTime(date);
                    }
                } catch (Exception e) {
                    Log.e("EditFoodFragment", "日期解析错误", e);
                }
            }
        }
    }

    // 更新食物项目
    private void updateFoodItem() {
        // 获取输入数据
        String storeName = etStoreName.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        String notesText = etNotes.getText().toString().trim();
        String locationText = etLocation.getText().toString().trim();
        float rating = ratingBar.getRating();
        
        // 验证输入
        if (TextUtils.isEmpty(storeName)) {
            Toast.makeText(requireContext(), "请输入店名", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final double price;
        if (!TextUtils.isEmpty(priceText)) {
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效的价格", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            price = 0.0;
        }
        
        // 格式化日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String selectedDateStr = dateFormat.format(selectedDate.getTime());
        
        // 获取标签
        List<String> tags = getSelectedTags();
        final String tagsString = String.join(",", tags);
        
        // 如果有照片待上传，则先上传照片
        if (photoUri != null && imageChanged) {
            try {
                // 读取照片数据
                InputStream inputStream = requireContext().getContentResolver().openInputStream(photoUri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] fileBytes = byteBuffer.toByteArray();
                String fileName = "food_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
                
                // 上传照片
                AppwriteWrapper.getInstance().uploadFile(
                    "67c2de08001a22001a6c", // FOOD_IMAGES_BUCKET_ID
                    fileName,
                    fileBytes,
                    fileId -> {
                        // 获取文件URL
                        final String uploadedImageUrl = AppwriteWrapper.getInstance().getFilePreviewUrl(
                            "67c2de08001a22001a6c", // FOOD_IMAGES_BUCKET_ID
                            fileId
                        );
                        
                        // 更新到数据库
                        saveToDatabase(storeName, selectedDateStr, uploadedImageUrl, rating, price, tagsString, notesText, locationText);
                    },
                    error -> {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "上传图片失败: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                );
            } catch (Exception e) {
                Toast.makeText(requireContext(), "读取图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // 没有新的照片，使用原来的图片URL
            String imageUrl = foodItemToEdit != null ? foodItemToEdit.getImageUrl() : "";
            saveToDatabase(storeName, selectedDateStr, imageUrl, rating, price, tagsString, notesText, locationText);
        }
    }

    // 保存到数据库
    private void saveToDatabase(String storeName, String time, String imageUrl, float rating, double price, String tags, String notes, String location) {
        // 获取当前用户ID
        String userId = AppwriteWrapper.getInstance().getCurrentUserId();
        
        // 获取文档ID
        String documentId = foodItemToEdit.getDocumentId();
        
        // 调用AppwriteWrapper更新记录
        AppwriteWrapper.getInstance().updateFoodItem(
            userId,
            documentId,
            storeName,
            time,
            imageUrl,
            rating,
            price,
            tags,
            notes,
            location,
            document -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "更新成功", Toast.LENGTH_SHORT).show();
                    
                    // 刷新首页数据
                    if (getActivity() != null && getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshHomeFragment();
                    }
                    
                    // 返回上一页
                    requireActivity().onBackPressed();
                });
            },
            error -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "更新失败: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            imageChanged = true; // 标记图片已更改
            
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 处理拍照结果
                photoContainer.setBackgroundResource(0); // 清除默认背景
                ImageView imageView = new ImageView(requireContext());
                imageView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageURI(photoUri);
                
                // 清除photoContainer中的所有视图并添加imageView
                photoContainer.removeAllViews();
                photoContainer.addView(imageView);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // 处理从相册选择的结果
                photoUri = data.getData();
                
                // 更新photoContainer的背景为选择的照片
                photoContainer.setBackgroundResource(0); // 清除默认背景
                ImageView imageView = new ImageView(requireContext());
                imageView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageURI(photoUri);
                
                // 清除photoContainer中的所有视图并添加imageView
                photoContainer.removeAllViews();
                photoContainer.addView(imageView);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，启动选择器
                dispatchPickPictureIntent();
            } else {
                // 权限被拒绝
                Toast.makeText(requireContext(), "需要存储权限才能选择照片", 
                              Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                // 更新选择的日期
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // 更新日期显示
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDate.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置最大日期为当前日期（不允许选择未来日期）
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }

    private List<String> getSelectedTags() {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && child.getId() != R.id.chip_add) {
                tags.add(((Chip) child).getText().toString());
            }
        }
        return tags;
    }

    private void showAddTagDialog() {
        // 使用AlertDialog.Builder替代，避免样式问题
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("添加标签");
        
        // 使用自定义布局
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_tag, null);
        builder.setView(dialogView);
        
        // 获取布局中的控件
        TextInputEditText inputTag = dialogView.findViewById(R.id.et_tag);
        ChipGroup quickTagsGroup = dialogView.findViewById(R.id.quick_tags_group);
        
        // 设置快速标签的点击事件
        for (int i = 0; i < quickTagsGroup.getChildCount(); i++) {
            View child = quickTagsGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    inputTag.setText(chip.getText().toString().replace("#", ""));
                });
            }
        }
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String tagText = inputTag.getText().toString().trim();
            if (!TextUtils.isEmpty(tagText)) {
                addTag(tagText);
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }

    private void addTag(String tagText) {
        Chip chip = new Chip(requireContext());
        chip.setText(tagText);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        
        if (chipAdd != null) {
            chipGroup.addView(chip, chipGroup.indexOfChild(chipAdd));
        } else {
            chipGroup.addView(chip);
        }
    }

    private void showPhotoOptions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("添加照片");
        
        String[] options = {"拍照", "从相册选择"};
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // 拍照
                dispatchTakePictureIntent();
            } else {
                // 从相册选择
                checkStoragePermissionAndPickImage();
            }
        });
        
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.tastylog.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void checkStoragePermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    100);
        } else {
            dispatchPickPictureIntent();
        }
    }

    private void dispatchPickPictureIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void loadImage(String imageUrl) {
        // Implementation of loadImage method
    }

    private void setupTags(List<String> tags) {
        // 清除chipGroup中除了"添加标签"按钮外的所有标签
        for (int i = chipGroup.getChildCount() - 1; i >= 0; i--) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && child.getId() != R.id.chip_add) {
                chipGroup.removeView(child);
            }
        }
        
        // 添加已有的标签
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                // 如果标签不为空且不是默认的"添加标签"
                if (!TextUtils.isEmpty(tag) && !tag.equals("添加标签")) {
                    // 确保标签以#开头
                    String tagText = tag.startsWith("#") ? tag : "#" + tag;
                    addTag(tagText);
                }
            }
        }
    }

    @Override
    public String getToolbarTitle() {
        return "编辑美食";
    }
} 