package com.example.tastylog;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.tastylog.adapter.FoodCardAdapter;
import com.example.tastylog.decoration.SpaceItemDecoration;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.fragment.AddFoodFragment;
import com.example.tastylog.fragment.FoodDetailFragment;
import com.example.tastylog.fragment.FavoriteFragment;
import com.example.tastylog.fragment.HomeFragment;
import com.example.tastylog.fragment.MineFragment;
import com.example.tastylog.fragment.StatsFragment;

import com.example.tastylog.AppwriteWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 使用 Java 包装类初始化 Appwrite
        AppwriteWrapper.init(getApplicationContext());

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
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_stats) {
                switchFragment(new StatsFragment());
                return true;
            } else if (id == R.id.nav_favorite) {
                switchFragment(new FavoriteFragment());
                return true;
            } else if (id == R.id.nav_mine) {
                switchFragment(new MineFragment());
                return true;
            }
            return false;
        });

        // 设置悬浮按钮
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            new AddFoodFragment().show(getSupportFragmentManager(), "add_food");
        });

        // 默认显示首页
        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
        }
    }

    public void openFoodDetail(FoodItem foodItem) {
        Intent intent = FoodDetailActivity.newIntent(this, foodItem);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(0, 0)
            .replace(R.id.container, fragment)
            .commit();
    }
} 