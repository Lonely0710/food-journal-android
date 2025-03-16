package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.tastylog.R;
import com.example.tastylog.MainActivity;
import com.example.tastylog.adapter.FoodCardAdapter;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.decoration.SpaceItemDecoration;
import com.example.tastylog.data.FoodRepository;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import com.example.tastylog.utils.FragmentUtils;

public class HomeFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private FoodCardAdapter adapter;
    private LottieAnimationView loadingAnimation;
    private LinearLayout emptyView;
    private Button btnAddFood;
    
    // 统计栏的TextView
    private TextView tvTotalSpending;
    private TextView tvAverageRating;
    private TextView tvRecordCount;
    
    // 日期格式化
    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // 初始化视图
        recyclerView = view.findViewById(R.id.recycler_view);
        loadingAnimation = view.findViewById(R.id.loading_animation);
        emptyView = view.findViewById(R.id.empty_view);
        btnAddFood = view.findViewById(R.id.btn_add_food);
        
        // 初始化统计栏的TextView
        tvTotalSpending = view.findViewById(R.id.tv_total_spending);
        tvAverageRating = view.findViewById(R.id.tv_average_rating);
        tvRecordCount = view.findViewById(R.id.tv_record_count);
        
        // 设置当前月份标题
        TextView tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        if (tvCurrentMonth != null) {
            tvCurrentMonth.setText(yearMonthFormat.format(new Date()));
        }
        
        // 初始化 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 添加间距装饰器
        int spaceInPixels = getResources().getDimensionPixelSize(R.dimen.card_margin);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spaceInPixels));
        
        // 初始化适配器
        adapter = new FoodCardAdapter();
        recyclerView.setAdapter(adapter);
        
        // 设置点击事件
        adapter.setOnItemClickListener(foodItem -> {
            // 不再直接使用FragmentManager，而是通过MainActivity打开详情页
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFoodDetail(foodItem);
            }
        });
        
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "FoodJournal";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 移除重复的初始化代码，只保留onCreateView中的初始化
    }

    // 添加刷新数据的方法
    public void refreshData() {
        // 显示加载动画
        showLoading();
        
        FoodRepository repository = FoodRepository.getInstance(requireContext());
        repository.getAllFoodItems(new FoodRepository.FoodListCallback() {
            @Override
            public void onFoodListLoaded(List<FoodItem> foodItems) {
                requireActivity().runOnUiThread(() -> {
                    // 更新统计栏
                    updateStatistics(foodItems);
                    
                    // 更新RecyclerView
                    adapter.setFoodList(foodItems);
                    
                    // 隐藏加载动画
                    hideLoading();
                    
                    // 如果没有数据，显示空状态视图
                    if (foodItems.isEmpty()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                FragmentUtils.safeUIAction(HomeFragment.this, () -> {
                    loadingAnimation.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "加载数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }, "HomeFragment");
            }
        });
    }
    
    // 计算并更新统计数据
    private void updateStatistics(List<FoodItem> foodItems) {
        // 获取当前年月
        String currentYearMonth = dateFormat.format(new Date());
        
        double totalSpending = 0;
        double totalRating = 0;
        int ratingCount = 0;
        int recordCount = 0;
        
        // 遍历所有食物记录，计算统计数据
        for (FoodItem item : foodItems) {
            // 检查是否是当前月份的记录
            if (item.getTime() != null && item.getTime().startsWith(currentYearMonth)) {
                recordCount++;
                
                // 计算总消费
                try {
                    String price = item.getPrice();
                    if (price != null && !price.isEmpty()) {
                        // 移除价格中的货币符号和其他非数字字符
                        price = price.replaceAll("[^0-9.]", "");
                        if (!price.isEmpty()) {
                            totalSpending += Double.parseDouble(price);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的价格
                }
                
                // 计算总评分
                float rating = item.getRating();
                if (rating > 0) {
                    totalRating += rating;
                    ratingCount++;
                }
            }
        }
        
        // 计算平均评分
        double averageRating = ratingCount > 0 ? totalRating / ratingCount : 0;
        
        // 格式化数据
        DecimalFormat priceFormat = new DecimalFormat("#,##0");
        DecimalFormat ratingFormat = new DecimalFormat("0.0");
        
        // 更新UI
        if (tvTotalSpending != null) {
            tvTotalSpending.setText("¥" + priceFormat.format(totalSpending));
        }
        
        if (tvAverageRating != null) {
            tvAverageRating.setText(ratingFormat.format(averageRating));
        }
        
        if (tvRecordCount != null) {
            tvRecordCount.setText(recordCount + "餐");
        }
    }
    
    // 显示加载动画
    private void showLoading() {
        if (loadingAnimation != null) {
            loadingAnimation.setVisibility(View.VISIBLE);
        }
        
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }
    
    // 隐藏加载动画
    private void hideLoading() {
        if (loadingAnimation != null) {
            loadingAnimation.setVisibility(View.GONE);
        }
    }
    
    // 显示空状态视图
    private void showEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }
    
    // 隐藏空状态视图
    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
} 