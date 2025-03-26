package com.example.tastylog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
            FoodItem foodItem = (FoodItem) getIntent().getSerializableExtra(EXTRA_FOOD_ITEM);
            
            if (foodItem != null) {
                Log.d("FoodDetailActivity", "接收到FoodItem: " + 
                      "ID=" + foodItem.getId() + 
                      ", 文档ID=" + foodItem.getDocumentId() + 
                      ", 标题=" + foodItem.getTitle());
            } else {
                Log.e("FoodDetailActivity", "错误: 接收到空的FoodItem");
            }
            
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

    public void deleteFoodItem(FoodItem foodItem) {
        // 获取当前用户ID
        String userId = AppwriteWrapper.getInstance().getCurrentUserId();
        
        // 使用文档ID而不是food_id
        String documentId = foodItem.getDocumentId();
        
        // 添加更多日志输出
        Log.d("FoodDetailActivity", "正在删除美食记录: ID=" + foodItem.getId() + 
              ", 文档ID=" + documentId + 
              ", 标题=" + foodItem.getTitle());
        
        if (documentId == null || documentId.isEmpty()) {
            Log.e("FoodDetailActivity", "错误: 文档ID为空");
            Toast.makeText(this, "无法删除：文档ID为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 调用 Appwrite 删除文档
        AppwriteWrapper.getInstance().deleteFoodItem(
            userId,
            documentId,  // 使用文档ID而不是food_id
            () -> {
                // 删除成功
                Log.d("FoodDetailActivity", "删除成功: documentId=" + documentId);
                runOnUiThread(() -> {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    
                    // 返回上一页
                    finish();
                    
                    // 刷新首页列表
                    MainActivity mainActivity = MainActivity.getInstance();
                    if (mainActivity != null) {
                        mainActivity.refreshHomeFragment();
                    }
                });
            },
            error -> {
                // 删除失败
                Log.e("FoodDetailActivity", "删除失败: " + error.getMessage(), error);
                runOnUiThread(() -> {
                    Toast.makeText(this, "删除失败: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        );
    }
} 