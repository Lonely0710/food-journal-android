package com.example.tastylog.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.airbnb.lottie.LottieAnimationView;
import com.example.tastylog.Appwrite;
import com.example.tastylog.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.tastylog.AppwriteWrapper;
import com.example.tastylog.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.tastylog.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.appwrite.models.User;
import io.appwrite.models.Document;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class MineFragment extends BaseFragment {
    private static final String TAG = "MineFragment";
    private ImageView ivUserAvatar;
    private TextView tvUserName;
    private LottieAnimationView loadingAnimation;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Uri photoUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        
        // 查找include的布局中的控件
        View userInfoView = view.findViewById(R.id.layout_mine_content);
        ivUserAvatar = userInfoView.findViewById(R.id.ivUserAvatar);
        tvUserName = userInfoView.findViewById(R.id.tvUserName);
        loadingAnimation = userInfoView.findViewById(R.id.loadingAnimation);
        
        // 显示加载状态
        showLoadingState();
        
        // 初始化退出登录按钮
        View logoutButton = view.findViewById(R.id.ll_logout);
        logoutButton.setOnClickListener(v -> showLogoutDialog());
        
        // 添加头像点击事件
        ivUserAvatar.setOnClickListener(v -> showPhotoOptions());
        
        // 添加用户名点击事件
        tvUserName.setOnClickListener(v -> showEditNameDialog());
        
        // 加载用户信息
        loadUserInfo();
        
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "Profile";
    }
    
    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        if (tvUserName != null) tvUserName.setVisibility(View.INVISIBLE);
        if (loadingAnimation != null) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        }
    }
    
    /**
     * 显示加载完成状态
     */
    private void showLoadedState(String username) {
        if (loadingAnimation != null) {
            loadingAnimation.pauseAnimation();
            loadingAnimation.setVisibility(View.GONE);
        }
        
        if (tvUserName != null) {
            tvUserName.setText(username);
            tvUserName.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载用户信息 - 使用回调版本
     */
    private void loadUserInfo() {
        if (!isAdded() || getActivity() == null) return;
        
        Log.d(TAG, "开始加载用户信息...");
        
        // 使用回调版本代替直接调用协程函数
        Appwrite.INSTANCE.getCurrentUserWithCallback(
            userData -> {
                if (getActivity() != null && isAdded()) {
                    Log.d(TAG, "成功获取用户数据: " + userData.toString());
                    
                    // 遍历所有键值对以检查 - 修复泛型问题
                    for (String key : userData.keySet()) {
                        Log.d(TAG, "用户数据字段 - " + key + ": " + userData.get(key));
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        // 直接从用户数据中获取信息
                        String name = (String) userData.get("name");
                        String avatarUrl = (String) userData.get("avatarUrl");
                        
                        Log.d(TAG, "提取的用户名: " + name);
                        Log.d(TAG, "提取的头像URL: " + avatarUrl);
                        
                        // 更新UI，显示加载完成状态
                        showLoadedState(name);
                        loadUserAvatar(avatarUrl);
                    });
                }
                return null;
            },
            error -> {
                Log.e(TAG, "获取用户信息失败", error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoadedState("访客");
                        loadUserAvatar(null);
                    });
                }
                return null;
            }
        );
    }
    
    /**
     * 加载用户头像 - 添加渐变效果
     */
    private void loadUserAvatar(String avatarUrl) {
        if (getActivity() == null || !isAdded()) return;
        
        Log.d(TAG, "加载头像: " + avatarUrl);
        
        // 添加默认灰色头像
        RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar);
        
        // 使用渐变动画效果
        Glide.with(this)
            .load(avatarUrl)
            .apply(options)
            .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(300))
            .circleCrop()
            .into(ivUserAvatar);
    }

    private void showLogoutDialog() {
            new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppTheme)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    showLoadingState();
                    AppwriteWrapper.logout(
                        () -> {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.slide_down_out);
                            getActivity().finish();
                        },
                        e -> {
                            showLoadedState(tvUserName.getText().toString());
                            Toast.makeText(getContext(), "登出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    );
                })
                .setNegativeButton("取消", null)
            .show();
    }

    // 添加图片选择对话框
    private void showPhotoOptions() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("修改头像")
               .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent();
                    } else {
                        checkStoragePermissionAndPickImage();
                    }
                })
               .show();
    }

    // 添加拍照方法
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // 修改 checkStoragePermissionAndPickImage 方法，参考 AddFoodFragment 的权限检查逻辑
    private void checkStoragePermissionAndPickImage() {
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

    // 修改 dispatchPickPictureIntent 方法，使用与 AddFoodFragment 相同的方式
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

    // 添加修改用户名对话框
    private void showEditNameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        EditText etNewName = dialogView.findViewById(R.id.et_new_name);
        etNewName.setText(tvUserName.getText());
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("修改用户名")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String newName = etNewName.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    updateUserName(newName);
                }
            })
            .setNegativeButton("取消", null)
                .show();
    }

    // 修改上传头像方法，添加资源关闭和错误处理
    private void uploadUserAvatar(Uri photoUri) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteBuffer = null;
        try {
            // 读取图片数据
            inputStream = requireContext().getContentResolver().openInputStream(photoUri);
            byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] fileBytes = byteBuffer.toByteArray();
            
            // 检查文件大小
            if (fileBytes.length > 5 * 1024 * 1024) { // 5MB限制
                Toast.makeText(requireContext(), "图片太大，请选择较小的图片", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String fileName = "avatar_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
            
            // 显示上传中
            showLoadingState();
            
            // 确保在主线程中执行UI操作
            requireActivity().runOnUiThread(() -> {
                // 上传头像
                Appwrite.INSTANCE.uploadAvatar(
                    fileName,
                    fileBytes,
                    fileId -> {
                        String avatarUrl = Appwrite.INSTANCE.getFilePreviewUrl(
                            AppConfig.FOOD_IMAGES_BUCKET_ID,
                            fileId);
                        updateUserAvatar(avatarUrl);
                        return null;
                    },
                    error -> {
                        requireActivity().runOnUiThread(() -> {
                            showLoadedState(tvUserName.getText().toString());
                            Toast.makeText(requireContext(), "上传头像失败: " + error.getMessage(), 
                                         Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    }
                );
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "处理图片失败: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
        } finally {
            // 确保关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (byteBuffer != null) {
                    byteBuffer.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "关闭流失败", e);
            }
        }
    }

    // 修改更新用户头像URL方法，确保在主线程中执行UI操作
    private void updateUserAvatar(String avatarUrl) {
        requireActivity().runOnUiThread(() -> {
            Appwrite.INSTANCE.updateUserAvatar(
                avatarUrl,
                () -> {
                    requireActivity().runOnUiThread(() -> {
                        showLoadedState(tvUserName.getText().toString());
                        loadUserAvatar(avatarUrl);
                        Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                },
                error -> {
                    requireActivity().runOnUiThread(() -> {
                        showLoadedState(tvUserName.getText().toString());
                        Toast.makeText(requireContext(), "更新失败: " + error.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    });
                    return null;
                }
            );
        });
    }

    // 修改更新用户名方法，确保在主线程中执行UI操作
    private void updateUserName(String newName) {
        showLoadingState();
        requireActivity().runOnUiThread(() -> {
            Appwrite.INSTANCE.updateUserName(
                newName, 
                () -> {
                    requireActivity().runOnUiThread(() -> {
                        showLoadedState(newName);
                        Toast.makeText(requireContext(), "用户名更新成功", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                },
                error -> {
                    requireActivity().runOnUiThread(() -> {
                        showLoadedState(tvUserName.getText().toString());
                        Toast.makeText(requireContext(), "更新失败: " + error.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    });
                    return null;
                }
            );
        });
    }

    // 添加处理图片选择结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageUri = photoUri;
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
            }
            
            if (imageUri != null) {
                uploadUserAvatar(imageUri);
            }
        }
    }

    // 添加createImageFile方法
    private File createImageFile() throws IOException {
        // 创建图片文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // 获取应用私有的外部存储目录
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        // 创建临时文件
        return File.createTempFile(
            imageFileName,  /* 前缀 */
            ".jpg",        /* 后缀 */
            storageDir     /* 目录 */
        );
    }
} 