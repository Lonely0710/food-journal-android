package com.example.tastylog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class FoodRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_DATE_HEADER = 0;
    public static final int VIEW_TYPE_FOOD_ITEM = 1;

    private static final String[] WEEKDAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    private List<Object> items = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());

    // 设置数据并按日期分组
    public void setData(List<FoodItem> foodItems) {
        // 按日期分组
        Map<String, List<FoodItem>> groupedItems = new HashMap<>();
        Map<String, Double> dailyExpenses = new HashMap<>();

        for (FoodItem item : foodItems) {
            try {
                String dateStr = item.getTime().split(" ")[0]; // 获取日期部分
                if (!groupedItems.containsKey(dateStr)) {
                    groupedItems.put(dateStr, new ArrayList<>());
                    dailyExpenses.put(dateStr, 0.0);
                }
                groupedItems.get(dateStr).add(item);
                
                // 计算每日总支出
                try {
                    String priceStr = item.getPrice();
                    if (priceStr != null && !priceStr.isEmpty()) {
                        priceStr = priceStr.replaceAll("[^0-9.]", "");
                        if (!priceStr.isEmpty()) {
                            double price = Double.parseDouble(priceStr);
                            dailyExpenses.put(dateStr, dailyExpenses.get(dateStr) + price);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略价格解析错误
                }
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }

        // 按日期降序排序
        TreeMap<String, List<FoodItem>> sortedGroups = new TreeMap<>(Collections.reverseOrder());
        sortedGroups.putAll(groupedItems);

        // 构建最终的项目列表
        items.clear();
        for (Map.Entry<String, List<FoodItem>> entry : sortedGroups.entrySet()) {
            String dateStr = entry.getKey();
            List<FoodItem> dailyItems = entry.getValue();
            
            // 添加日期头部
            items.add(new DateHeader(dateStr, dailyExpenses.getOrDefault(dateStr, 0.0)));
            
            // 为每个日期组按时间排序
            Collections.sort(dailyItems, (item1, item2) -> item2.getTime().compareTo(item1.getTime()));
            
            // 添加该日期的所有食物项
            items.addAll(dailyItems);
        }
        
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DateHeader ? VIEW_TYPE_DATE_HEADER : VIEW_TYPE_FOOD_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = inflater.inflate(R.layout.item_date_header_wx_style, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_food_record_wx_style, parent, false);
            return new FoodItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            DateHeader header = (DateHeader) items.get(position);
            ((DateHeaderViewHolder) holder).bind(header);
        } else if (holder instanceof FoodItemViewHolder) {
            FoodItem foodItem = (FoodItem) items.get(position);
            ((FoodItemViewHolder) holder).bind(foodItem);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 日期头部的ViewHolder
    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvWeekday;
        TextView tvDailyExpense;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvWeekday = itemView.findViewById(R.id.tv_weekday);
            tvDailyExpense = itemView.findViewById(R.id.tv_daily_expense);
        }

        public void bind(DateHeader header) {
            try {
                // 解析日期以获取星期信息
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(header.getDate());
                if (date != null) {
                    // 设置日期
                    tvDate.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(date));
                    
                    // 设置星期
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0-6
                    tvWeekday.setText(WEEKDAYS[dayOfWeek]);
                    
                    // 设置每日支出
                    tvDailyExpense.setText(String.format(Locale.getDefault(), "支出: ¥%.2f", header.getDailyExpense()));
                }
            } catch (ParseException e) {
                // 解析失败，使用原始日期字符串
                tvDate.setText(header.getDate());
                tvWeekday.setText("");
                tvDailyExpense.setText(String.format(Locale.getDefault(), "支出: ¥%.2f", header.getDailyExpense()));
            }
        }
    }

    // 食物项的ViewHolder
    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName;
        TextView tvRestaurant;
        TextView tvPrice;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.iv_food_image);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvRestaurant = itemView.findViewById(R.id.tv_restaurant);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        public void bind(FoodItem foodItem) {
            tvFoodName.setText(foodItem.getTitle());
            tvRestaurant.setText(foodItem.getLocation() != null ? foodItem.getLocation() : "");
            
            // 格式化价格
            String price = foodItem.getPrice();
            if (price != null && !price.isEmpty()) {
                if (!price.startsWith("¥")) {
                    price = "¥" + price;
                }
                tvPrice.setText(price);
            } else {
                tvPrice.setText("¥0.00");
            }
            
            // 加载图片
            if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
                Glide.with(ivFoodImage.getContext())
                     .load(foodItem.getImageUrl())
                     .placeholder(R.drawable.placeholder_food)
                     .centerCrop()
                     .into(ivFoodImage);
            } else {
                ivFoodImage.setImageResource(R.drawable.placeholder_food);
            }
        }
    }

    // 日期头部数据类
    static class DateHeader {
        private String date;
        private double dailyExpense;

        public DateHeader(String date, double dailyExpense) {
            this.date = date;
            this.dailyExpense = dailyExpense;
        }

        public String getDate() {
            return date;
        }

        public double getDailyExpense() {
            return dailyExpense;
        }
    }
} 