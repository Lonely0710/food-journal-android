package com.example.tastylog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FoodCardAdapter extends RecyclerView.Adapter<FoodCardAdapter.ViewHolder> {
    private List<FoodItem> foodList = new ArrayList<>();

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvTitle;
        TextView tvTime;
        TextView tvRating;
        TextView tvPrice;
        ChipGroup chipGroup;

        ViewHolder(View view) {
            super(view);
            ivFood = view.findViewById(R.id.iv_food);
            tvTitle = view.findViewById(R.id.tv_title);
            tvTime = view.findViewById(R.id.tv_time);
            tvRating = view.findViewById(R.id.tv_rating);
            tvPrice = view.findViewById(R.id.tv_price);
            chipGroup = view.findViewById(R.id.chip_group);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        
        // 设置数据
        holder.tvTitle.setText(item.getTitle());
        holder.tvTime.setText(item.getTime());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        holder.tvPrice.setText(item.getPrice());
        
        // 设置标签
        holder.chipGroup.removeAllViews();
        for (String tag : item.getTags()) {
            Chip chip = new Chip(holder.chipGroup.getContext());
            chip.setText(tag);
            chip.setTextAppearance(R.style.FoodCard_Tag);
            holder.chipGroup.addView(chip);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void setFoodList(List<FoodItem> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }
} 