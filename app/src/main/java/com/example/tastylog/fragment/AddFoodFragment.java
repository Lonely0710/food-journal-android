package com.example.tastylog.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.tastylog.R;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class AddFoodFragment extends BottomSheetDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AddFood_BottomSheet);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // 获取dialog
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 获取底部容器
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 获取behavior
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                
                // 设置固定高度为屏幕高度的 2/3
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int targetHeight = (screenHeight * 2) / 3;
                
                // 设置布局参数
                ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
                params.height = targetHeight;
                bottomSheet.setLayoutParams(params);
                
                // 禁用拖动
                behavior.setDraggable(false);
                // 禁用折叠
                behavior.setSkipCollapsed(true);
                // 设置展开状态
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // 固定内容
                behavior.setFitToContents(true);
                // 设置峰值高度
                behavior.setPeekHeight(targetHeight);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_food, container, false);
        
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        // 返回按钮
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            // 获取 BottomSheet
            View bottomSheet = ((View) view.getParent());
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            
            // 添加状态回调
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // 可以在这里添加其他动画效果
                }
            });

            // 设置可以隐藏
            behavior.setHideable(true);
            // 执行隐藏动画
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        // 保存按钮也使用同样的动画效果
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // TODO: 保存逻辑
            Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
            
            View bottomSheet = ((View) view.getParent());
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        // 照片上传区域点击事件
        view.findViewById(R.id.layout_photo).setOnClickListener(v -> {
            showPhotoPickerDialog();
        });
    }

    private void showPhotoPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_photo_picker, null);
        
        // 拍照按钮
        dialogView.findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            // TODO: 启动相机
            dialog.dismiss();
        });

        // 从相册选择按钮
        dialogView.findViewById(R.id.btn_pick_photo).setOnClickListener(v -> {
            // TODO: 打开图库
            dialog.dismiss();
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }
} 