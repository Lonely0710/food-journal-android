package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastylog.MainActivity;
import com.example.tastylog.R;
import com.example.tastylog.adapter.FavoriteAdapter;
import com.example.tastylog.model.FavoriteItem;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        
        // 设置标题
        if (getActivity() != null) {
            getActivity().setTitle("收藏");
        }

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.rv_favorites);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        // 添加测试数据
        List<FavoriteItem> favorites = getTestData();
        
        // 设置适配器
        adapter = new FavoriteAdapter(favorites, item -> {
            // 处理点击事件
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFoodDetail(null); // 需要转换FavoriteItem到FoodItem
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<FavoriteItem> getTestData() {
        List<FavoriteItem> list = new ArrayList<>();
        list.add(new FavoriteItem("1", "意大利风情餐厅", "https://example.com/image1.jpg", "意大利料理", 4.8f, "2024-01-15"));
        list.add(new FavoriteItem("2", "日式寿司店", "https://example.com/image2.jpg", "日本料理", 4.6f, "2024-01-14"));
        list.add(new FavoriteItem("3", "粤式茶点", "https://example.com/image3.jpg", "粤式点心", 4.7f, "2024-01-13"));
        list.add(new FavoriteItem("4", "泰国餐厅", "https://example.com/image4.jpg", "泰国菜", 4.5f, "2024-01-12"));
        return list;
    }
} 