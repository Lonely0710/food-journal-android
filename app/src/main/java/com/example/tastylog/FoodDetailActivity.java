package com.example.tastylog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastylog.fragment.FoodDetailFragment;
import com.example.tastylog.model.FoodItem;

public class FoodDetailActivity extends AppCompatActivity {

    private static final String EXTRA_FOOD_ITEM = "food_item";

    public static Intent newIntent(Context context, FoodItem foodItem) {
        Intent intent = new Intent(context, FoodDetailActivity.class);
        intent.putExtra(EXTRA_FOOD_ITEM, (java.io.Serializable) foodItem);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // 设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.orange_500));

        if (savedInstanceState == null) {
            FoodItem foodItem = getIntent().getParcelableExtra(EXTRA_FOOD_ITEM);
            FoodDetailFragment fragment = FoodDetailFragment.newInstance(foodItem);
            
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        // 添加返回动画
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 系统返回键也使用相同动画
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
} 