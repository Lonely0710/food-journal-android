package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tastylog.R;
import com.example.tastylog.adapter.FoodImageAdapter;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.appbar.MaterialToolbar;

public class FoodDetailFragment extends Fragment {

    private FoodItem foodItem;

    public static FoodDetailFragment newInstance(FoodItem foodItem) {
        FoodDetailFragment fragment = new FoodDetailFragment();
        Bundle args = new Bundle();
        // TODO: 将FoodItem数据传入Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // 设置ViewPager
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        // TODO: 实现图片适配器

        // 设置底部按钮事件
        view.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            // TODO: 跳转到编辑页面
        });

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            showDeleteConfirmDialog();
        });

        view.findViewById(R.id.btn_share).setOnClickListener(v -> {
            // TODO: 实现分享功能
            Toast.makeText(requireContext(), "分享功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条美食记录吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                // TODO: 实现删除逻辑
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            })
            .setNegativeButton("取消", null)
            .show();
    }
} 