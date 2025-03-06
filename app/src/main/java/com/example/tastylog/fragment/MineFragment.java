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
import android.app.AlertDialog;

import androidx.annotation.NonNull;
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

import java.util.List;
import java.util.Map;
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
        
        initSwitches(view);
        initButtons(view);
        
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

    private void initSwitches(View view) {
        SwitchMaterial switchBackup = view.findViewById(R.id.switch_backup);
        SwitchMaterial switchWatermark = view.findViewById(R.id.switch_watermark);
        
        switchBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(), "自动备份：" + isChecked, Toast.LENGTH_SHORT).show();
        });

        switchWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(), "水印功能：" + isChecked, Toast.LENGTH_SHORT).show();
        });
    }

    private void initButtons(View view) {
        // 导出格式按钮
        View btnExportFormat = view.findViewById(R.id.btn_export_format);
        TextView tvExportFormat = btnExportFormat.findViewById(R.id.tv_export_format);
        
        btnExportFormat.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add("PDF");
            popup.getMenu().add("Excel");
            popup.getMenu().add("CSV");
            popup.setOnMenuItemClickListener(item -> {
                tvExportFormat.setText(item.getTitle());
                return true;
            });
            popup.show();
        });

        // 主题颜色按钮
        View btnThemeColor = view.findViewById(R.id.btn_theme_color);
        btnThemeColor.setOnClickListener(v -> {
            // TODO: 实现主题颜色选择
            Toast.makeText(requireContext(), "主题颜色选择功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 添加登出按钮点击事件
        View logoutButton = view.findViewById(R.id.ll_logout);
        logoutButton.setOnClickListener(v -> {
            // 显示确认对话框
            new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppTheme)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 显示加载状态
                    showLoadingState();
                    
                    // 调用登出方法
                    AppwriteWrapper.logout(
                        // 成功回调
                        () -> {
                            // 跳转到登录页面
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.slide_down_out);
                            getActivity().finish();
                        },
                        // 失败回调
                        e -> {
                            showLoadedState(tvUserName.getText().toString());
                            Toast.makeText(getContext(), "登出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    );
                })
                .setNegativeButton("取消", null)
                .setBackground(getResources().getDrawable(R.drawable.bg_dialog))
                .show();
        });
    }
} 