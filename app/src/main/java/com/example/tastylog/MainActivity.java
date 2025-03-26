package com.example.tastylog;

import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.tastylog.fragment.AddFoodFragment;
import com.example.tastylog.fragment.FoodDetailFragment;
import com.example.tastylog.fragment.HomeFragment;
import com.example.tastylog.fragment.MapFragment;
import com.example.tastylog.fragment.MineFragment;
import com.example.tastylog.fragment.StatsFragment;
import com.example.tastylog.model.FoodItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * 应用主界面
 * 作为容器管理各个功能Fragment的加载和切换
 */
public class MainActivity extends AppCompatActivity {
    
    private FloatingActionButton fab;
    private BottomNavigationView bottomNav;
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        // 使用 Java 包装类初始化 Appwrite
        AppwriteWrapper.init(getApplicationContext());

        // 设置状态栏颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.orange_500));

        // 设置底部导航
        bottomNav = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fab_add);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_stats) {
                switchFragment(new StatsFragment());
                return true;
            } else if (itemId == R.id.nav_favorite) {
                switchFragment(new MapFragment());
                return true;
            } else if (itemId == R.id.nav_mine) {
                switchFragment(new MineFragment());
                return true;
            }
            return false;
        });

        // 设置悬浮按钮
        fab.setOnClickListener(v -> {
            // 使用Fragment替换，而不是启动新Activity
            AddFoodFragment addFoodFragment = new AddFoodFragment();
            
            // 隐藏FAB（在事务开始前）
            fab.hide();
            
            // 执行事务
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, addFoodFragment)
                .addToBackStack(null)
                .commit();
        });

        // 默认显示首页
        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
        }
        
        // 添加回退栈监听器，确保返回时正确显示/隐藏FAB
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // 延迟执行，确保Fragment事务完成
            new Handler().postDelayed(this::updateFabVisibility, 100);
        });
    }

    // 修改openFoodDetail方法，使用Fragment替换而不是启动新Activity
    public void openFoodDetail(FoodItem foodItem) {
        // 创建FoodDetailFragment并传递参数
        Bundle bundle = new Bundle();
        bundle.putSerializable("food_item", foodItem);
        
        FoodDetailFragment detailFragment = new FoodDetailFragment();
        detailFragment.setArguments(bundle);
        
        // 隐藏FAB（在事务开始前）
        fab.hide();
        
        // 使用Fragment事务替换当前Fragment
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit();
    }

    private void switchFragment(Fragment fragment) {
        // 在执行新事务前，立即更新FAB状态
        if (fragment instanceof HomeFragment) {
            fab.show();
        } else {
            fab.hide();
        }
        
        // 使用handler推迟事务执行，避免可能的冲突
        new Handler().post(() -> {
            // 清除回退栈中的所有Fragment
            if (getSupportFragmentManager().isStateSaved()) {
                return; // 如果状态已保存，不执行操作
            }
            
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            
            // 替换当前Fragment
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        });
    }
    
    /**
     * 更新FAB的显示状态
     * 在HomeFragment中显示，在其他Fragment中隐藏
     */
    public void updateFabVisibility() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        
        // 简化逻辑：只在HomeFragment中显示FAB，其他所有Fragment中隐藏
        if (currentFragment instanceof HomeFragment) {
            fab.show();
        } else {
            fab.hide();
        }
    }
    
    @Override
    public void onBackPressed() {
        // 获取回退栈中的Fragment数量
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        
        if (backStackCount > 0) {
            // 如果回退栈不为空，执行正常的回退操作
            if (!getSupportFragmentManager().isStateSaved()) {
                getSupportFragmentManager().popBackStack();
                
                // 在事务完成后确保更新FAB的可见性（使用延迟确保事务完成）
                new Handler().postDelayed(this::updateFabVisibility, 100);
            }
        } else {
            // 如果回退栈为空，检查当前Fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            
            if (!(currentFragment instanceof HomeFragment)) {
                // 如果当前不是HomeFragment，切换到HomeFragment
                bottomNav.setSelectedItemId(R.id.nav_home); // 这会触发底部导航栏的选中事件，进而调用switchFragment
            } else {
                // 如果当前已经是HomeFragment，执行正常的回退操作（退出应用）
                super.onBackPressed();
            }
        }
    }
    
    public void refreshHomeFragment() {
        // 添加这一行以获取当前Fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).refreshData();
        } else {
            // 如果当前不是HomeFragment，切换到HomeFragment
            switchFragment(new HomeFragment());
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) {
            instance = null;
        }
    }
} 