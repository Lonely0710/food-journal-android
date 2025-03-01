package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tastylog.R;
import com.example.tastylog.MainActivity;
import com.example.tastylog.adapter.FoodCardAdapter;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.decoration.SpaceItemDecoration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private FoodCardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(16));
        
        // 设置适配器
        adapter = new FoodCardAdapter();
        recyclerView.setAdapter(adapter);
        
        // 设置点击事件
        adapter.setOnItemClickListener(foodItem -> {
            // 打开详情页
            ((MainActivity) requireActivity()).openFoodDetail(foodItem);
        });

        // 加载测试数据
        loadTestData();
        
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "FoodJournal";
    }

    private void loadTestData() {
        List<FoodItem> foodList = new ArrayList<>();
        
        // 创建测试数据 - 使用更丰富的数据
        FoodItem item1 = new FoodItem(
            "https://example.com/food1.jpg", // 实际开发中替换为真实图片URL
            "山城火锅",
            "07-15 晚餐",
            4.5f,
            "¥98/人",
            Arrays.asList("麻辣", "朋友聚餐")
        );
        foodList.add(item1);

        FoodItem item2 = new FoodItem(
            "https://example.com/food2.jpg",
            "星巴克（朝阳门店）",
            "07-14 下午茶",
            3.8f,
            "¥45",
            Arrays.asList("咖啡", "安静")
        );
        foodList.add(item2);

        FoodItem item3 = new FoodItem(
            "https://example.com/food3.jpg",
            "寿司の神",
            "07-13 午餐",
            4.7f,
            "¥128/人",
            Arrays.asList("日料", "约会")
        );
        foodList.add(item3);

        FoodItem item4 = new FoodItem(
            "https://example.com/food4.jpg",
            "老北京炸酱面",
            "07-12 午餐",
            4.2f,
            "¥22",
            Arrays.asList("面食", "传统")
        );
        foodList.add(item4);
        
        FoodItem item5 = new FoodItem(
            "https://example.com/food5.jpg",
            "粤式茶餐厅",
            "07-10 早餐",
            4.3f,
            "¥68/人",
            Arrays.asList("粤菜", "家庭聚餐")
        );
        foodList.add(item5);
        
        FoodItem item6 = new FoodItem(
            "https://example.com/food6.jpg",
            "川湘菜馆",
            "07-08 晚餐",
            4.6f,
            "¥88/人",
            Arrays.asList("川菜", "湘菜", "辣")
        );
        foodList.add(item6);

        adapter.setFoodList(foodList);
    }
} 