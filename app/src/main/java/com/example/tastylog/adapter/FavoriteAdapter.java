package com.example.tastylog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tastylog.R;
import com.example.tastylog.model.FavoriteItem;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private List<FavoriteItem> favorites;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FavoriteItem item);
    }

    public FavoriteAdapter(List<FavoriteItem> favorites, OnItemClickListener listener) {
        this.favorites = favorites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fav_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = favorites.get(position);
        
        // 加载图片
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .into(holder.ivRestaurant);

        // 设置文本
        holder.tvName.setText(item.getName());
        holder.tvRating.setText(String.format("%.1f", item.getRating()));
        holder.tvCuisineType.setText(item.getCuisineType());
        holder.tvFavDate.setText("收藏于 " + item.getFavoriteDate());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurant;
        TextView tvName;
        TextView tvRating;
        TextView tvCuisineType;
        TextView tvFavDate;

        ViewHolder(View view) {
            super(view);
            ivRestaurant = view.findViewById(R.id.iv_restaurant);
            tvName = view.findViewById(R.id.tv_restaurant_name);
            tvRating = view.findViewById(R.id.tv_rating);
            tvCuisineType = view.findViewById(R.id.tv_cuisine_type);
            tvFavDate = view.findViewById(R.id.tv_fav_date);
        }
    }
} 