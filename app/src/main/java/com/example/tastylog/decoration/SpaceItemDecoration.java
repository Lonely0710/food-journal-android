package com.example.tastylog.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public SpaceItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = spacing;
        outRect.right = spacing;
        outRect.bottom = spacing;
        
        // 第一个item添加顶部间距
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = spacing;
        }
    }
} 