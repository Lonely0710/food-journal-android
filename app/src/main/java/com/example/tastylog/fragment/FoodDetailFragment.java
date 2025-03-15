package com.example.tastylog.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.tastylog.MainActivity;
import com.example.tastylog.R;
import com.example.tastylog.adapter.FoodImageAdapter;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class FoodDetailFragment extends Fragment {

    private FoodItem foodItem;

    public static FoodDetailFragment newInstance(FoodItem foodItem) {
        FoodDetailFragment fragment = new FoodDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("food_item", foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_detail, container, false);
        
        // 获取传递的食物数据
        Bundle args = getArguments();
        if (args != null && args.containsKey("food_item")) {
            foodItem = (FoodItem) args.getSerializable("food_item");
        }
        
        if (foodItem != null) {
            // 使用布局中实际存在的ID
            TextView tvTitle = view.findViewById(R.id.tv_title);
            TextView tvTime = view.findViewById(R.id.tv_time);
            TextView tvPrice = view.findViewById(R.id.tv_price);
            TextView tvRating = view.findViewById(R.id.tv_rating);
            TextView tvNotes = view.findViewById(R.id.tv_notes); // 用于显示备注/content
            RatingBar ratingBar = view.findViewById(R.id.rating_bar); // 添加RatingBar
            
            // 设置内容
            if (tvTitle != null) tvTitle.setText(foodItem.getTitle());
            if (tvTime != null) tvTime.setText(foodItem.getTime());
            if (tvPrice != null) tvPrice.setText(foodItem.getPrice());
            if (tvRating != null) tvRating.setText(String.valueOf(foodItem.getRating()));
            if (ratingBar != null) ratingBar.setRating(foodItem.getRating()); // 设置RatingBar的值
            
            // 显示content字段（备注）
            if (tvNotes != null) {
                String content = foodItem.getContent();
                if (content != null && !content.isEmpty()) {
                    tvNotes.setText(content);
                    tvNotes.setVisibility(View.VISIBLE);
                } else {
                    tvNotes.setText("暂无备注");
                }
            }
            
            // 加载图片
            ViewPager2 viewPager = view.findViewById(R.id.view_pager);
            if (viewPager != null && foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
                // 在onViewCreated中实现图片加载
            }
            
            // 设置标签
            ChipGroup chipGroup = view.findViewById(R.id.chip_group);
            if (chipGroup != null && foodItem.getTags() != null && !foodItem.getTags().isEmpty()) {
                chipGroup.removeAllViews();
                for (String tag : foodItem.getTags()) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(tag);
                    chip.setClickable(false);
                    chipGroup.addView(chip);
                }
            }
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置RatingBar显示实际评分
        if (foodItem != null) {
            RatingBar ratingBar = view.findViewById(R.id.rating_bar);
            if (ratingBar != null) {
                ratingBar.setRating(foodItem.getRating());
            }
        }

        // 设置Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // 使用FragmentManager的popBackStack方法返回上一个Fragment
                requireActivity().getSupportFragmentManager().popBackStack();
                
                // 延迟更新FAB可见性，确保事务完成
                new Handler().postDelayed(() -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateFabVisibility();
                    }
                }, 100);
            });
        }

        // 设置ViewPager
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        if (viewPager != null && foodItem != null) {
            // 创建适配器并设置图片列表
            FoodImageAdapter adapter = new FoodImageAdapter();
            List<String> imageList = new ArrayList<>();
            
            // 确保只有当图片URL存在且非空时才添加
            if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
                imageList.add(foodItem.getImageUrl());
            }
            
            adapter.setImageUrls(imageList);
            viewPager.setAdapter(adapter);
        }

        // 设置底部按钮事件
        View btnEdit = view.findViewById(R.id.btn_edit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                // TODO: 跳转到编辑页面
            });
        }

        View btnDelete = view.findViewById(R.id.btn_delete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                showDeleteConfirmDialog();
            });
        }

        View btnShare = view.findViewById(R.id.btn_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                // TODO: 实现分享功能
                Toast.makeText(requireContext(), "分享功能开发中", Toast.LENGTH_SHORT).show();
            });
        }
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