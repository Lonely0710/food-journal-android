package com.example.tastylog.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FoodCardAdapter extends RecyclerView.Adapter<FoodCardAdapter.ViewHolder> {
    private List<FoodItem> foodList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodItem foodItem);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFoodList(List<FoodItem> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
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
        FoodItem foodItem = foodList.get(position);
        
        holder.tvTitle.setText(foodItem.getTitle());
        holder.tvTime.setText(foodItem.getTime());
        
        float rating = foodItem.getRating();
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
        
        if (holder.ratingBar != null) {
            holder.ratingBar.setRating(rating);
        }
        
        if (holder.tvRating != null) {
            holder.tvRating.setText(String.format("%.1f", rating));
        }
        
        String price = foodItem.getPrice();
        if (price == null || price.isEmpty() || price.equals("¥")) {
            price = "¥0";
        }
        
        if (holder.tvPrice != null) {
            holder.tvPrice.setText(price);
        }
        
        holder.chipGroup.removeAllViews();
        for (String tag : foodItem.getTags()) {
            Chip chip = new Chip(holder.chipGroup.getContext());
            chip.setText(tag);
            chip.setChipBackgroundColorResource(R.color.colorChipBackground);
            chip.setTextColor(ContextCompat.getColor(holder.chipGroup.getContext(), R.color.colorChipText));
            holder.chipGroup.addView(chip);
        }
        
        if (!TextUtils.isEmpty(foodItem.getImageUrl())) {
            Glide.with(holder.ivFood.getContext())
                .load(foodItem.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.error_food)
                .centerCrop()
                .into(holder.ivFood);
        } else {
            holder.ivFood.setImageResource(R.drawable.placeholder_food);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(foodItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvTitle;
        TextView tvTime;
        RatingBar ratingBar;
        TextView tvRating;
        TextView tvPrice;
        ChipGroup chipGroup;
        
        ViewHolder(View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.iv_food);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPrice = itemView.findViewById(R.id.tv_price);
            chipGroup = itemView.findViewById(R.id.chip_group);
        }
    }
} 