package com.example.tastylog;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.tastylog.adapter.FoodCardAdapter;
import com.example.tastylog.decoration.SpaceItemDecoration;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.fragment.AddFoodFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置状态栏颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.orange_500));

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置底部导航
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            // 处理导航项选择
            return true;
        });

        // 设置悬浮按钮
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            new AddFoodFragment().show(getSupportFragmentManager(), "add_food");
        });

        initFoodList();
    }

    private void initFoodList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 创建测试数据
        List<FoodItem> testData = new ArrayList<>();
        testData.add(new FoodItem(
            null,
            "山城火锅",
            "07-15 晚餐",
            4.5f,
            "¥98/人",
            Arrays.asList("#麻辣", "#朋友聚餐")
        ));
        testData.add(new FoodItem(
            null,
            "星巴克（朝阳门店）",
            "07-14 下午茶",
            3.8f,
            "¥45",
            Arrays.asList("#咖啡", "#工作间隙")
        ));

        // 设置适配器
        FoodCardAdapter adapter = new FoodCardAdapter();
        adapter.setFoodList(testData);
        recyclerView.setAdapter(adapter);

        // 设置item间距
        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
    }
} 