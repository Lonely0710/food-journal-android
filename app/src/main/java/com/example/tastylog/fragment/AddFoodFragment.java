package com.example.tastylog.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.tastylog.AppwriteWrapper;
import com.example.tastylog.MainActivity;
import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.utils.BitmapUtil;
import com.example.tastylog.config.AppConfig;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 添加食物Fragment
 * 允许用户创建新的食物记录和上传图片
 */
public class AddFoodFragment extends BaseFragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // 修改变量名以匹配布局文件中的ID
    private TextInputEditText etStoreName; // 原etTitle
    private TextInputEditText etPrice;
    private TextInputEditText etNotes;
    private TextInputEditText etLocation; // 新增位置输入框变量
    private TextInputEditText etDate;
    private RatingBar ratingBar;
    private ImageView ivFoodPhoto; // 将使用布局中的photo_container
    private ChipGroup chipGroup;
    private Uri photoUri;
    private FrameLayout photoContainer;
    private Chip chipAdd; // 用于添加标签
    private Calendar selectedDate = Calendar.getInstance(); // 默认为当前日期

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_food, container, false);
        
        // 初始化视图
        etStoreName = view.findViewById(R.id.et_store_name);
        etPrice = view.findViewById(R.id.et_price);
        etNotes = view.findViewById(R.id.et_notes);
        etLocation = view.findViewById(R.id.et_location); // 初始化位置输入框
        etDate = view.findViewById(R.id.et_date); // 初始化日期选择框
        ratingBar = view.findViewById(R.id.rating_bar);
        chipGroup = view.findViewById(R.id.chip_group);
        photoContainer = view.findViewById(R.id.layout_photo);
        chipAdd = view.findViewById(R.id.chip_add);
        
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
        btnSave.setOnClickListener(v -> {
            saveFoodItem();
        });
        
        // 设置照片容器点击事件
        photoContainer.setOnClickListener(v -> {
            showPhotoOptions();
        });
        
        // 设置添加标签点击事件
        chipAdd.setOnClickListener(v -> {
            showAddTagDialog();
        });
        
        // 设置日期选择框的初始值为当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(dateFormat.format(selectedDate.getTime()));
        
        // 设置日期选择框点击事件
        etDate.setOnClickListener(v -> showDatePickerDialog());
        
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "添加美食";
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // 确保输入法可用
        if (etStoreName != null) {
            etStoreName.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etStoreName, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void showAddTagDialog() {
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
                    // 当点击快速标签时，只是将文本填入输入框，而不是直接添加标签
                    String tagText = chip.getText().toString();
                    inputTag.setText(tagText.replace("#", "")); // 移除#号，因为在addTag时会自动添加
                    inputTag.setSelection(inputTag.length()); // 将光标移到文本末尾
                });
            }
        }
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String tagText = inputTag.getText().toString().trim();
            if (!TextUtils.isEmpty(tagText)) {
                // 只在用户点击确定时添加标签
                addTag(tagText);
            }
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 添加标签到标签组
     * 
     * @param tagText 标签文本
     */
    private void addTag(String tagText) {
        // 确保标签以#开头
        String finalTagText = tagText.startsWith("#") ? tagText : "#" + tagText;
        
        // 检查标签是否已存在
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).getText().toString().equals(finalTagText)) {
                Toast.makeText(requireContext(), "该标签已存在", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 创建新的Chip
        Chip chip = new Chip(requireContext());
        chip.setText(finalTagText);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        
        // 将新标签添加到chipAdd之前
        if (chipAdd != null) {
            chipGroup.addView(chip, chipGroup.indexOfChild(chipAdd));
        } else {
            chipGroup.addView(chip);
        }
    }

    private void showPhotoOptions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("选择照片来源");
        String[] options = {"拍照", "从相册选择"};
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // 拍照
                dispatchTakePictureIntent();
            } else {
                // 从相册选择
                // 检查权限
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), 
                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                100);
                        return;
                    }
                } else if (ContextCompat.checkSelfPermission(requireContext(), 
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            100);
                    return;
                }
                dispatchPickPictureIntent();
            }
        });
        
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // 创建保存照片的文件
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File storageDir = requireActivity().getExternalFilesDir(null);
                photoFile = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
                
                // 保存文件路径用于后续使用
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.tastylog.fileprovider", photoFile);
                
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                Toast.makeText(requireContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchPickPictureIntent() {
        // 使用ACTION_GET_CONTENT - 这是最通用的方式
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        
        try {
            startActivityForResult(Intent.createChooser(intent, "选择照片"), REQUEST_IMAGE_PICK);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(requireContext(), "没有找到图片浏览器应用", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        if (TextUtils.isEmpty(etStoreName.getText())) {
            etStoreName.setError("请输入店铺名称");
            isValid = false;
        }
        
        if (ratingBar.getRating() == 0) {
            Toast.makeText(requireContext(), "请给美食评分", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }

    private void saveFoodItem() {
        if (!validateForm()) {
            return;
        }
        
        // 获取用户输入
        final String storeName = etStoreName.getText().toString().trim();
        final String priceText = etPrice.getText().toString().trim();
        final String notesText = etNotes.getText().toString().trim();
        final String locationText = etLocation.getText().toString().trim();
        final float rating = ratingBar.getRating();

        // 验证必填字段
        if (TextUtils.isEmpty(storeName)) {
            etStoreName.setError("请输入店名");
            return;
        }

        // 处理价格
        double price = 0.0;
        if (!TextUtils.isEmpty(priceText)) {
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                etPrice.setError("请输入有效价格");
                return;
            }
        }
        
        // 创建一个final变量用于lambda表达式
        final double finalPrice = price;

        // 获取选中的标签
        List<String> tags = getSelectedTags();
        final String tagsString = TextUtils.join(",", tags);

        // 使用所选日期而不是当前时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String selectedDateStr = dateFormat.format(selectedDate.getTime());

        // 如果有照片，上传照片
        String imageUrl = null;
        if (photoUri != null) {
            try {
                // 使用BitmapUtil压缩图片
                byte[] fileBytes = BitmapUtil.getCompressedImageBytes(requireContext(), photoUri, 800, 800, 85);
                
                if (fileBytes == null) {
                    Toast.makeText(requireContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 获取文件的实际MIME类型
                String mimeType = requireContext().getContentResolver().getType(photoUri);
                
                // 生成带有正确扩展名的文件名
                String extension = ".jpg"; // 默认扩展名
                if (mimeType != null) {
                    if (mimeType.equals("image/jpeg")) {
                        extension = ".jpg";
                    } else if (mimeType.equals("image/png")) {
                        extension = ".png";
                    } else if (mimeType.equals("image/gif")) {
                        extension = ".gif";
                    }
                }
                
                String fileName = "food_" + System.currentTimeMillis() + extension;
                
                // 显示上传中提示
                Toast.makeText(requireContext(), "正在上传图片...", Toast.LENGTH_SHORT).show();
                
                // 调用AppwriteWrapper上传文件
                AppwriteWrapper.getInstance().uploadFile(
                    AppConfig.FOOD_IMAGES_BUCKET_ID,
                    fileName,
                    fileBytes,
                    fileId -> {
                        // 获取文件URL并立即创建最终变量
                        final String uploadedImageUrl = AppwriteWrapper.getInstance().getFilePreviewUrl(
                            AppConfig.FOOD_IMAGES_BUCKET_ID,
                            fileId
                        );
                        
                        // 保存食物记录，使用最终变量
                        saveToDatabase(storeName, selectedDateStr, uploadedImageUrl, rating, finalPrice, tagsString, notesText, locationText);
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
            // 没有照片，直接保存记录（使用空图片URL）
            final String emptyImageUrl = "";
            saveToDatabase(storeName, selectedDateStr, emptyImageUrl, rating, finalPrice, tagsString, notesText, locationText);
        }
    }

    private void saveToDatabase(String storeName, String time, String imageUrl, float rating, double price, String tags, String notes, String location) {
        // 创建FoodItem对象
        FoodItem foodItem = new FoodItem();
        foodItem.setTitle(storeName);
        foodItem.setPrice(String.valueOf(price));
        foodItem.setRating(rating);
        foodItem.setNotes(notes);
        foodItem.setTime(time);
        foodItem.setTags(List.of(tags.split(",")));
        foodItem.setImageUrl(imageUrl);
        foodItem.setLocation(location);
        
        // 获取当前用户ID
        String userId = AppwriteWrapper.getInstance().getCurrentUserId();
        
        // 调用AppwriteWrapper保存食物记录
        AppwriteWrapper.getInstance().addFoodItem(
            userId,
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
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    
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
                    Toast.makeText(requireContext(), "保存失败: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 处理拍照结果
                // 更新photoContainer的背景为拍摄的照片
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置返回按钮
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            // 使用FragmentManager的popBackStack方法返回上一个Fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    /**
     * 显示日期选择对话框
     */
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

    /**
     * 获取所有选中的标签
     */
    private List<String> getSelectedTags() {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            // 只收集非"添加标签"按钮的Chip
            if (child instanceof Chip && child.getId() != R.id.chip_add) {
                tags.add(((Chip) child).getText().toString());
            }
        }
        return tags;
    }
} 