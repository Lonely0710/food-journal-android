package com.example.tastylog.fragment;

import android.app.DatePickerDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

import com.example.tastylog.R;
import com.example.tastylog.adapter.FoodRecordAdapter;
import com.example.tastylog.data.FoodRepository;
import com.example.tastylog.data.FoodRepository.FoodListCallback;
import com.example.tastylog.model.FoodItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 统计Fragment
 * 展示食物记录的统计数据
 */
public class StatsFragment extends BaseFragment {

    private static final String TAG = "StatsFragment";
    
    // UI 组件
    private TabLayout viewSwitcher;
    private NestedScrollView scrollView;
    private MaterialButton btnDateRange;
    private MaterialButton btnPriceRange;
    
    // 图表组件
    private LineChart lineChart;
    private PieChart pieChart;
    
    // 日期格式化
    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // 筛选条件
    private Date startDate;
    private Date endDate;
    private double minPrice = 0;
    private double maxPrice = 300;

    // 列表相关
    private RecyclerView recyclerViewFoodRecords;
    private FoodRecordAdapter foodRecordAdapter;

    // 添加列表视图中的按钮引用
    private Button btnListDateFilter;
    private Button btnListFilter;

    // 添加筛选相关的属性
    private String currentFoodType = "全部";
    private int minRating = 0;
    private int maxRating = 5;

    // 添加位置和标签筛选相关的变量
    private String locationFilter = "";
    private Set<String> selectedTags = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        
        // 设置标题
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("数据统计");
        }
        
        // 初始化视图组件
        initViews(view);
        
        // 初始化日期范围（默认为当前月）
        initDateRange();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 强制检查并设置正确的视图状态
        View listViewContainer = view.findViewById(R.id.list_view_container);
        View chartViewContainer = view.findViewById(R.id.chart_view_container);
        
        if (viewSwitcher != null && viewSwitcher.getSelectedTabPosition() == 0) {
            // 如果选中的是第一个tab（列表视图）
            if (listViewContainer != null) listViewContainer.setVisibility(View.VISIBLE);
            if (chartViewContainer != null) chartViewContainer.setVisibility(View.GONE);
        } else if (viewSwitcher != null && viewSwitcher.getSelectedTabPosition() == 1) {
            // 如果选中的是第二个tab（图表视图）
            if (listViewContainer != null) listViewContainer.setVisibility(View.GONE);
            if (chartViewContainer != null) chartViewContainer.setVisibility(View.VISIBLE);
        }
        
        // 设置日期范围按钮点击事件
        btnDateRange.setOnClickListener(v -> showDateRangePicker());
        
        // 设置价格范围按钮点击事件
        btnPriceRange.setOnClickListener(v -> showPriceRangePicker());
        
        // 加载数据
        loadData();
    }

    private void initViews(View view) {
        viewSwitcher = view.findViewById(R.id.view_switcher);
        scrollView = view.findViewById(R.id.stats_scroll_view);
        btnDateRange = view.findViewById(R.id.btn_date_range);
        btnPriceRange = view.findViewById(R.id.btn_price_range);
        
        // 获取视图容器
        View listViewContainer = view.findViewById(R.id.list_view_container);
        View chartViewContainer = view.findViewById(R.id.chart_view_container);
        
        // 设置默认视图状态 - 列表视图可见，图表视图隐藏
        if (listViewContainer != null) listViewContainer.setVisibility(View.VISIBLE);
        if (chartViewContainer != null) chartViewContainer.setVisibility(View.GONE);
        
        // 设置图表切换监听器
        viewSwitcher.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toggleViewVisibility(tab.getPosition());
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要处理
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });
        
        // 确保默认选中第一个tab（列表视图）
        if (viewSwitcher.getTabCount() > 0) {
            viewSwitcher.selectTab(viewSwitcher.getTabAt(0));
        }
        
        // 添加这些视图的检查，如果布局已更新但没有这些ID，防止崩溃
        View lineChartContainer = null;
        View pieChartContainer = null;
        
        try {
            lineChartContainer = view.findViewById(R.id.line_chart_container);
            pieChartContainer = view.findViewById(R.id.pie_chart_container);
        } catch (Exception e) {
            Log.e(TAG, "找不到图表容器: " + e.getMessage());
        }
        
        // 设置默认显示折线图容器
        if (lineChartContainer != null) lineChartContainer.setVisibility(View.VISIBLE);
        if (pieChartContainer != null) pieChartContainer.setVisibility(View.GONE);

        ImageView lineChartPlaceholder = view.findViewById(R.id.line_chart);
        if (lineChartPlaceholder != null) {
            ViewGroup lineChartParent = (ViewGroup) lineChartPlaceholder.getParent();
            int lineChartIndex = lineChartParent.indexOfChild(lineChartPlaceholder);
            
            // 创建LineChart并添加到布局中
            lineChart = new LineChart(requireContext());
            lineChart.setLayoutParams(lineChartPlaceholder.getLayoutParams());
            lineChartParent.removeView(lineChartPlaceholder);
            lineChartParent.addView(lineChart, lineChartIndex);
            
            // 配置LineChart
            configureLineChart();
        }
        
        // 同样处理PieChart
        ImageView pieChartPlaceholder = view.findViewById(R.id.pie_chart);
        if (pieChartPlaceholder != null) {
            ViewGroup pieChartParent = (ViewGroup) pieChartPlaceholder.getParent();
            int pieChartIndex = pieChartParent.indexOfChild(pieChartPlaceholder);
            
            // 创建PieChart并添加到布局中
            pieChart = new PieChart(requireContext());
            pieChart.setLayoutParams(pieChartPlaceholder.getLayoutParams());
            pieChartParent.removeView(pieChartPlaceholder);
            pieChartParent.addView(pieChart, pieChartIndex);
            
            // 配置PieChart
            configurePieChart();
        }

        // 初始化列表相关组件
        recyclerViewFoodRecords = view.findViewById(R.id.recycler_view_food_records);
        if (recyclerViewFoodRecords != null) {
            recyclerViewFoodRecords.setLayoutManager(new LinearLayoutManager(getContext()));
            
            // 初始化适配器
            foodRecordAdapter = new FoodRecordAdapter();
            recyclerViewFoodRecords.setAdapter(foodRecordAdapter);
        }

        // 添加分割线装饰
        recyclerViewFoodRecords.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                // 仅在非日期头部项添加底部间距
                int position = parent.getChildAdapterPosition(view);
                if (position >= 0 && foodRecordAdapter.getItemViewType(position) != 
                        FoodRecordAdapter.VIEW_TYPE_DATE_HEADER) {
                    outRect.bottom = 0; // 不需要额外间距，已在item布局中添加分割线
                }
            }
        });

        // 初始化列表视图中的筛选按钮
        btnListDateFilter = view.findViewById(R.id.btn_date_range_list);
        btnListFilter = view.findViewById(R.id.btn_filter);
        
        if (btnListDateFilter != null) {
            btnListDateFilter.setOnClickListener(v -> showListDatePicker());
        }
        
        if (btnListFilter != null) {
            btnListFilter.setOnClickListener(v -> showListFilterDialog());
        }
    }

    private void configureLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraOffsets(10, 10, 10, 10);
        
        // 设置X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.gray_600));
        
        // 设置左侧Y轴
        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.gray_600));
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(getResources().getColor(R.color.gray_200));
        
        // 禁用右侧Y轴
        lineChart.getAxisRight().setEnabled(false);
        
        // 设置图例
        lineChart.getLegend().setTextColor(getResources().getColor(R.color.gray_600));
        lineChart.getLegend().setForm(Legend.LegendForm.LINE);
        
        // 设置动画
        lineChart.animateX(1000);
    }

    private void configurePieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(20, 10, 20, 10);  // 增加左右边距
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        
        // 设置中心空白区域
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.WHITE);
        pieChart.setHoleRadius(58f);  // 增大中心空洞
        pieChart.setTransparentCircleRadius(61f);
        
        // 中心文字设置
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("评分分布");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(getResources().getColor(R.color.gray_600));
        
        // 图例和标签设置
        pieChart.setEntryLabelColor(android.graphics.Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawEntryLabels(false);  // 不在饼图上直接显示标签
        
        // 禁用图例(使用自定义图例)
        pieChart.getLegend().setEnabled(false);
        
        // 其他设置
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
    }

    private void initDateRange() {
        // 设置默认日期范围为当前月份的第一天到最后一天
        Calendar calendar = Calendar.getInstance();
        
        // 设置为当月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();
        
        // 设置为当月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = calendar.getTime();
        
        // 更新按钮文本
        updateDateRangeButtonText();
    }

    private void updateDateRangeButtonText() {
        // 如果起止日期在同一月份，则只显示年月
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        
        if (startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
            startCalendar.get(Calendar.MONTH) == endCalendar.get(Calendar.MONTH)) {
            btnDateRange.setText(yearMonthFormat.format(startDate));
        } else {
            // 否则显示起止日期
            SimpleDateFormat shortFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            String startStr = shortFormat.format(startDate);
            String endStr = shortFormat.format(endDate);
            btnDateRange.setText(startStr + " - " + endStr);
        }
    }

    private void toggleViewVisibility(int tabPosition) {
        View listViewContainer = getView().findViewById(R.id.list_view_container);
        View chartViewContainer = getView().findViewById(R.id.chart_view_container);
        
        if (tabPosition == 0) { // 列表视图
            if (listViewContainer != null) listViewContainer.setVisibility(View.VISIBLE);
            if (chartViewContainer != null) chartViewContainer.setVisibility(View.GONE);
        } else { // 图表视图
            if (listViewContainer != null) listViewContainer.setVisibility(View.GONE);
            if (chartViewContainer != null) chartViewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showDateRangePicker() {
        // 显示日期选择对话框
        // 此处简化为只选择月份，实际应用中可以使用日期范围选择器或自定义对话框
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                // 设置选中月份的第一天到最后一天
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, month, 1);
                startDate = newCal.getTime();
                
                newCal.set(year, month, newCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = newCal.getTime();
                
                // 更新按钮文本
                updateDateRangeButtonText();
                
                // 重新加载数据
                loadData();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void showPriceRangePicker() {
        // 此处应显示价格范围选择对话框
        // 为简化示例，这里只是切换几个预设范围
        if (maxPrice == 300) {
            minPrice = 0;
            maxPrice = 100;
        } else if (maxPrice == 100) {
            minPrice = 0;
            maxPrice = 500;
        } else {
            minPrice = 0;
            maxPrice = 300;
        }
        
        // 更新按钮文本
        updatePriceRangeButtonText();
        
        // 重新加载数据
        loadData();
    }

    private void updatePriceRangeButtonText() {
        DecimalFormat format = new DecimalFormat("#,##0");
        btnPriceRange.setText("¥" + format.format(minPrice) + " - ¥" + format.format(maxPrice));
    }

    private void loadData() {
        showLoading();
        
        FoodRepository.getInstance(requireContext()).getAllFoodItems(new FoodListCallback() {
            @Override
            public void onFoodListLoaded(List<FoodItem> foodItems) {
                hideLoading();
                
                // 筛选数据
                List<FoodItem> filteredItems = filterFoodItems(foodItems);
                
                if (filteredItems.isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                    // 更新图表
                    updateCharts(filteredItems);
                    // 更新列表视图
                    updateListView(filteredItems);
                }
            }
            
            @Override
            public void onError(Exception e) {
                hideLoading();
                showToast("加载数据失败: " + e.getMessage());
                Log.e(TAG, "加载数据失败", e);
            }
        });
    }

    private List<FoodItem> filterFoodItems(List<FoodItem> foodItems) {
        List<FoodItem> result = new ArrayList<>();
        
        for (FoodItem item : foodItems) {
            // 检查日期范围
            if (startDate != null && endDate != null) {
                try {
                    Date itemDate = fullDateFormat.parse(item.getTime().split(" ")[0]);
                    if (itemDate == null || itemDate.before(startDate) || itemDate.after(endDate)) {
                        continue;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "日期解析错误", e);
                    continue;
                }
            }
            
            // 检查价格范围
            if (minPrice > 0 || maxPrice < 500) {
                try {
                    double price = Double.parseDouble(item.getPrice().replace("¥", ""));
                    if (price < minPrice || price > maxPrice) {
                        continue;
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    // 价格格式无效，跳过
                    continue;
                }
            }
            
            // 检查位置
            if (!locationFilter.isEmpty() && item.getLocation() != null) {
                if (!item.getLocation().toLowerCase().contains(locationFilter.toLowerCase())) {
                    continue;
                }
            }
            
            // 检查评分范围
            int rating = (int) item.getRating();
            if (rating < minRating || rating > maxRating) {
                continue;
            }
            
            // 检查标签
            if (!selectedTags.isEmpty() && item.getTags() != null) {
                boolean hasMatchingTag = false;
                for (String tag : item.getTags()) {
                    if (selectedTags.contains(tag)) {
                        hasMatchingTag = true;
                        break;
                    }
                }
                if (!hasMatchingTag) {
                    continue;
                }
            }
            
            // 通过所有筛选条件
            result.add(item);
        }
        
        return result;
    }

    /**
     * 更新图表
     * 根据筛选后的数据更新统计图表
     * 
     * @param foodItems 筛选后的食物记录列表
     */
    private void updateCharts(List<FoodItem> foodItems) {
        if (foodItems.isEmpty()) {
            showEmptyCharts();
            return;
        }
        
        // 更新消费趋势图
        updateSpendingTrendChart(foodItems);
        
        // 更新评分分布图
        updateRatingDistributionChart(foodItems);
    }

    private void updateSpendingTrendChart(List<FoodItem> foodItems) {
        // 按日期分组，计算每日消费
        Map<String, Double> dailySpending = new TreeMap<>(); // 使用TreeMap保证按日期排序
        
        SimpleDateFormat dayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        
        for (FoodItem item : foodItems) {
            try {
                // 解析日期
                Date itemDate = fullDateFormat.parse(item.getTime());
                if (itemDate == null) continue;
                
                String day = dayFormat.format(itemDate);
                
                // 解析价格
                double price = 0;
                try {
                    String priceStr = item.getPrice();
                    if (priceStr != null && !priceStr.isEmpty()) {
                        priceStr = priceStr.replaceAll("[^0-9.]", "");
                        if (!priceStr.isEmpty()) {
                            price = Double.parseDouble(priceStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的价格
                    continue;
                }
                
                // 累加到对应日期
                dailySpending.put(day, dailySpending.getOrDefault(day, 0.0) + price);
                
            } catch (ParseException e) {
                // 忽略日期解析错误
                Log.e(TAG, "日期解析错误: " + item.getTime(), e);
            }
        }
        
        // 准备折线图数据
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Double> entry : dailySpending.entrySet()) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            i++;
        }
        
        // 创建数据集
        LineDataSet dataSet = new LineDataSet(entries, "日消费金额");
        dataSet.setColor(getResources().getColor(R.color.orange_500));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.orange_500));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.orange_200));
        
        // 设置X轴标签
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        
        // 创建LineData对象并设置到图表
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        
        // 刷新图表
        lineChart.invalidate();
    }

    private void updateRatingDistributionChart(List<FoodItem> foodItems) {
        // 统计各评分数量
        Map<Float, Integer> ratingCounts = new HashMap<>();
        
        for (FoodItem item : foodItems) {
            float rating = item.getRating();
            if (rating > 0) {
                // 四舍五入到最近的0.5
                rating = Math.round(rating * 2) / 2.0f;
                ratingCounts.put(rating, ratingCounts.getOrDefault(rating, 0) + 1);
            }
        }
        
        // 准备饼图数据
        List<PieEntry> entries = new ArrayList<>();
        
        // 用一个TreeMap来对评分进行排序(从高到低)
        TreeMap<Float, Integer> sortedRatings = new TreeMap<>(Collections.reverseOrder());
        sortedRatings.putAll(ratingCounts);
        
        for (Map.Entry<Float, Integer> entry : sortedRatings.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey() + "星"));
        }
        
        // 创建数据集
        PieDataSet dataSet = new PieDataSet(entries, "评分分布");
        
        // 设置更和谐的颜色
        int[] MATERIAL_COLORS = {
            getResources().getColor(R.color.orange_500),
            getResources().getColor(R.color.orange_300),
            getResources().getColor(R.color.green_500),
            getResources().getColor(R.color.blue_500),
            getResources().getColor(R.color.purple_500)
        };
        dataSet.setColors(MATERIAL_COLORS);
        
        // 调整切片之间的间距
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);
        
        // 设置值的格式和位置
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineColor(getResources().getColor(R.color.gray_600));
        
        // 创建PieData对象并设置到图表
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(getResources().getColor(R.color.gray_800));
        
        // 设置数据
        pieChart.setData(pieData);
        
        // 更新图例
        updatePieChartLegends(sortedRatings);
        
        // 刷新图表
        pieChart.invalidate();
    }

    // 添加一个方法来更新自定义图例
    private void updatePieChartLegends(TreeMap<Float, Integer> ratingCounts) {
        // 获取各个图例视图
        View legend5 = getView().findViewById(R.id.legend_5_star);
        View legend4 = getView().findViewById(R.id.legend_4_star);
        View legend3 = getView().findViewById(R.id.legend_3_star);
        View legend2 = getView().findViewById(R.id.legend_2_star);
        
        // 隐藏所有图例
        if (legend5 != null) legend5.setVisibility(View.GONE);
        if (legend4 != null) legend4.setVisibility(View.GONE);
        if (legend3 != null) legend3.setVisibility(View.GONE);
        if (legend2 != null) legend2.setVisibility(View.GONE);
        
        // 根据实际数据显示对应图例
        int index = 0;
        for (Map.Entry<Float, Integer> entry : ratingCounts.entrySet()) {
            float rating = entry.getKey();
            int count = entry.getValue();
            
            View legendView = null;
            if (rating == 5.0f && legend5 != null) {
                legendView = legend5;
            } else if (rating == 4.0f && legend4 != null) {
                legendView = legend4;
            } else if (rating == 3.0f && legend3 != null) {
                legendView = legend3;
            } else if (rating == 2.0f && legend2 != null) {
                legendView = legend2;
            }
            
            if (legendView != null) {
                // 设置颜色指示器
                ImageView colorIndicator = legendView.findViewById(R.id.legend_color);
                if (colorIndicator != null && index < 5) {
                    int[] MATERIAL_COLORS = {
                        getResources().getColor(R.color.orange_500),
                        getResources().getColor(R.color.orange_300),
                        getResources().getColor(R.color.green_500),
                        getResources().getColor(R.color.blue_500),
                        getResources().getColor(R.color.purple_500)
                    };
                    colorIndicator.setColorFilter(MATERIAL_COLORS[index]);
                }
                
                // 设置评分文本
                TextView ratingText = legendView.findViewById(R.id.legend_text);
                if (ratingText != null) {
                    ratingText.setText(rating + "星评分");
                }
                
                // 设置数量
                TextView countText = legendView.findViewById(R.id.legend_count);
                if (countText != null) {
                    countText.setText(count + "条");
                }
                
                // 显示图例
                legendView.setVisibility(View.VISIBLE);
                index++;
            }
        }
    }

    private void showEmptyCharts() {
        // 清空图表数据
        lineChart.clear();
        pieChart.clear();
        
        // 显示空态提示
        lineChart.setNoDataText("暂无消费数据");
        pieChart.setNoDataText("暂无评分数据");
        
        // 刷新图表
        lineChart.invalidate();
        pieChart.invalidate();
    }

    private void showLoading() {
        if (getView() != null) {
            View loadingContainer = getView().findViewById(R.id.loading_container);
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideLoading() {
        if (getView() != null) {
            View loadingContainer = getView().findViewById(R.id.loading_container);
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected String getToolbarTitle() {
        return "Statistics";
    }

    // 添加更新列表视图的方法
    private void updateListView(List<FoodItem> foodItems) {
        if (foodRecordAdapter != null) {
            foodRecordAdapter.setData(foodItems);
        }
    }

    // 添加空视图相关方法
    private void showEmptyView() {
        View emptyView = getView().findViewById(R.id.empty_view);
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        View emptyView = getView().findViewById(R.id.empty_view);
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    // 添加提示方法
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // 显示列表日期选择对话框
    private void showListDatePicker() {
        // 创建日期选择器
        Calendar calendar = Calendar.getInstance();
        if (startDate != null) {
            calendar.setTime(startDate);
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                // 设置选中月份的第一天到最后一天
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, month, 1);
                startDate = newCal.getTime();
                
                newCal.set(year, month, newCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = newCal.getTime();
                
                // 更新按钮文本
                btnListDateFilter.setText(yearMonthFormat.format(startDate));
                
                // 重新加载数据
                loadData();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    // 显示列表筛选对话框
    private void showListFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_food_filter, null);
        
        // 获取价格范围滑块
        RangeSlider priceRangeSlider = dialogView.findViewById(R.id.price_range_slider);
        TextView tvPriceRange = dialogView.findViewById(R.id.tv_price_range);
        
        // 获取位置搜索框
        TextInputEditText etLocationSearch = dialogView.findViewById(R.id.et_location_search);
        
        // 获取评分范围滑块
        RangeSlider ratingRangeSlider = dialogView.findViewById(R.id.rating_range_slider);
        TextView tvRatingRange = dialogView.findViewById(R.id.tv_rating_range);
        
        // 获取标签选择器
        ChipGroup chipGroupTags = dialogView.findViewById(R.id.chip_group_tags);
        
        // 设置当前价格范围
        priceRangeSlider.setValues((float)minPrice, (float)maxPrice);
        tvPriceRange.setText(String.format("¥%.0f - ¥%.0f", minPrice, maxPrice));
        
        // 设置价格范围监听器
        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            float min = values.get(0);
            float max = values.get(1);
            tvPriceRange.setText(String.format("¥%.0f - ¥%.0f", min, max));
        });
        
        // 设置当前位置搜索
        etLocationSearch.setText(locationFilter);
        
        // 设置当前评分范围
        ratingRangeSlider.setValues((float)minRating, (float)maxRating);
        tvRatingRange.setText(String.format("%d - %d 星", minRating, maxRating));
        
        // 设置评分范围监听器
        ratingRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            int min = Math.round(values.get(0));
            int max = Math.round(values.get(1));
            tvRatingRange.setText(String.format("%d - %d 星", min, max));
        });
        
        // 设置当前标签选择
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            View child = chipGroupTags.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setChecked(selectedTags.contains(chip.getText().toString()));
            }
        }
        
        // 创建对话框
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("筛选条件")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                // 获取价格范围
                List<Float> priceValues = priceRangeSlider.getValues();
                minPrice = priceValues.get(0);
                maxPrice = priceValues.get(1);
                
                // 获取位置搜索
                locationFilter = etLocationSearch.getText().toString().trim();
                
                // 获取评分范围
                List<Float> ratingValues = ratingRangeSlider.getValues();
                minRating = Math.round(ratingValues.get(0));
                maxRating = Math.round(ratingValues.get(1));
                
                // 获取选中的标签
                selectedTags.clear();
                for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
                    View child = chipGroupTags.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (chip.isChecked()) {
                            selectedTags.add(chip.getText().toString());
                        }
                    }
                }
                
                // 更新筛选按钮文本
                updateFilterButtonText();
                
                // 重新加载数据
                loadData();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    // 获取ChipGroup中所有的Chip
    private List<Chip> getAllChips(ChipGroup chipGroup) {
        List<Chip> chips = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                chips.add((Chip) child);
            }
        }
        return chips;
    }

    // 更新筛选按钮文本
    private void updateFilterButtonText() {
        StringBuilder filterText = new StringBuilder();
        
        // 添加价格范围
        filterText.append("¥").append((int)minPrice).append("-").append((int)maxPrice);
        
        // 如果有位置筛选，添加位置信息
        if (!locationFilter.isEmpty()) {
            filterText.append(" | ").append(locationFilter);
        }
        
        // 如果评分范围不是0-5，添加评分范围
        if (minRating > 0 || maxRating < 5) {
            filterText.append(" | ").append(minRating).append("-").append(maxRating).append("星");
        }
        
        // 如果有选中的标签，添加标签数量
        if (!selectedTags.isEmpty()) {
            filterText.append(" | ").append(selectedTags.size()).append("个标签");
        }
        
        btnListFilter.setText(filterText.toString());
    }
} 