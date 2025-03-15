package com.example.tastylog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastylog.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FoodImageAdapter extends RecyclerView.Adapter<FoodImageAdapter.ViewHolder> {
    private List<String> imageUrls = new ArrayList<>();

    static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ViewHolder(View view) {
            super(view);
            photoView = view.findViewById(R.id.photo_view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (imageUrls != null && imageUrls.size() > 0 && position < imageUrls.size()) {
            String imageUrl = imageUrls.get(position);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // 使用Glide加载图片
                Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .centerCrop()
                    .into(holder.photoView);
            } else {
                // 如果URL为空，显示占位图
                holder.photoView.setImageResource(R.drawable.placeholder_food);
            }
        } else {
            // 没有图片时显示占位图
            holder.photoView.setImageResource(R.drawable.placeholder_food);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size() > 0 ? imageUrls.size() : 1; // 至少显示一张占位图
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        notifyDataSetChanged();
    }
} 