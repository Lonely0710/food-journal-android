package com.example.tastylog.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.tastylog.AppwriteWrapper;
import com.example.tastylog.model.FoodItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Map;

// 添加Document类的导入
import io.appwrite.models.Document;

public class FoodRepository {
    private static final String TAG = "FoodRepository";
    private static FoodRepository instance;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final List<FoodItem> cachedFoodItems = new ArrayList<>();
    private final AppwriteWrapper appwrite;

    private FoodRepository(Context context) {
        appwrite = AppwriteWrapper.getInstance();
    }

    public static synchronized FoodRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FoodRepository(context.getApplicationContext());
        }
        return instance;
    }

    public interface FoodListCallback {
        void onFoodListLoaded(List<FoodItem> foodItems);
        void onError(Exception e);
    }

    public interface FoodItemCallback {
        void onSuccess(FoodItem foodItem);
        void onError(Exception e);
    }

    // 获取所有食物记录
    public void getAllFoodItems(FoodListCallback callback) {
        executor.execute(() -> {
            try {
                // 获取当前用户ID
                String userId = AppwriteWrapper.getInstance().getCurrentUserId();
                
                // 如果没有用户ID，返回空列表
                if (TextUtils.isEmpty(userId)) {
                    callback.onFoodListLoaded(new ArrayList<>());
                    return;
                }
                
                // 从Appwrite获取数据
                fetchFoodItemsFromAppwrite(userId, callback);
                
                // 注释掉测试数据部分
                // List<FoodItem> foodItems = getTestData();
                // cachedFoodItems.clear();
                // cachedFoodItems.addAll(foodItems);
                // callback.onFoodListLoaded(foodItems);
            } catch (Exception e) {
                Log.e(TAG, "Error loading food items", e);
                callback.onError(e);
            }
        });
    }

    // 添加新的食物记录
    public void addFoodItem(FoodItem foodItem, FoodItemCallback callback) {
        executor.execute(() -> {
            try {
                // 转换为JSON
                JSONObject json = foodItemToJson(foodItem);
                
                // 调用Appwrite API创建文档
                // 这里简化处理，实际应该调用Appwrite API
                // appwrite.createDocument("foodItems", json.toString(), ...)
                
                // 更新缓存
                cachedFoodItems.add(foodItem);
                
                callback.onSuccess(foodItem);
            } catch (Exception e) {
                Log.e(TAG, "Error adding food item", e);
                callback.onError(e);
            }
        });
    }

    // 将FoodItem转换为JSON，匹配Appwrite数据库结构
    private JSONObject foodItemToJson(FoodItem foodItem) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("food_id", foodItem.getId());
        json.put("title", foodItem.getTitle());
        json.put("time", foodItem.getTime());
        json.put("rating", foodItem.getRating());
        
        // 价格转换为Double（如果可能）
        try {
            // 移除价格中的货币符号和"/人"等文本
            String priceStr = foodItem.getPrice().replaceAll("[^0-9.]", "");
            double priceValue = Double.parseDouble(priceStr);
            json.put("price", priceValue);
        } catch (NumberFormatException e) {
            // 如果转换失败，保存原始字符串
            json.put("price", foodItem.getPrice());
        }
        
        // 标签处理 - 数据库只支持单个标签，我们取第一个或连接所有标签
        List<String> tags = foodItem.getTags();
        if (tags != null && !tags.isEmpty()) {
            // 选项1：只使用第一个标签
            json.put("tag", tags.get(0));
            
            // 选项2：将所有标签连接为一个字符串（以逗号分隔）
            // json.put("tag", TextUtils.join(", ", tags));
        } else {
            json.put("tag", "");
        }
        
        // 图片URL - 使用img_url字段名
        json.put("img_url", foodItem.getImageUrl());
        
        // 添加用户ID - 这个需要从当前登录用户获取
        // 这里假设我们有一个方法可以获取当前用户ID
        String userId = AppwriteWrapper.getInstance().getCurrentUserId();
        json.put("user_id", userId);
        
        return json;
    }

    // 从Appwrite获取用户的食物记录
    private void fetchFoodItemsFromAppwrite(String userId, FoodListCallback callback) {
        // 调用Appwrite.kt中的方法
        AppwriteWrapper.getInstance().getUserFoodItems(
            userId,
            documents -> {
                List<FoodItem> foodItems = new ArrayList<>();
                for (Document<Map<String, Object>> document : documents) {
                    // 添加调试日志
                    Map<String, Object> data = document.getData();
                    Log.d(TAG, "Document ID: " + document.getId());
                    Log.d(TAG, "Document Data: " + data.toString());
                    if (data.containsKey("rating")) {
                        Log.d(TAG, "Rating: " + data.get("rating") + " (Class: " + data.get("rating").getClass().getName() + ")");
                    }
                    if (data.containsKey("price")) {
                        Log.d(TAG, "Price: " + data.get("price") + " (Class: " + data.get("price").getClass().getName() + ")");
                    }
                    
                    FoodItem item = documentToFoodItem(document);
                    foodItems.add(item);
                    
                    // 添加转换后的日志
                    Log.d(TAG, "Converted FoodItem: " + item.getTitle() + ", Rating: " + item.getRating() + ", Price: " + item.getPrice());
                }
                cachedFoodItems.clear();
                cachedFoodItems.addAll(foodItems);
                callback.onFoodListLoaded(foodItems);
            },
            error -> {
                Log.e(TAG, "Error fetching food items from Appwrite", error);
                callback.onError(error);
            }
        );
    }

    // 将Appwrite Document转换为FoodItem
    private FoodItem documentToFoodItem(Document<Map<String, Object>> document) {
        FoodItem item = new FoodItem();
        
        // 设置ID - 使用document.$id作为唯一标识符
        item.setId(document.getId());
        
        Map<String, Object> data = document.getData();
        
        // 设置标题
        if (data.containsKey("title")) {
            item.setTitle((String) data.get("title"));
        }
        
        // 设置时间
        if (data.containsKey("time")) {
            item.setTime((String) data.get("time"));
        }
        
        // 设置评分 - 使用正确的字段名
        if (data.containsKey("rating")) {
            Object ratingObj = data.get("rating");
            if (ratingObj instanceof Double) {
                item.setRating(((Double) ratingObj).floatValue());
            } else if (ratingObj instanceof Integer) {
                item.setRating(((Integer) ratingObj).floatValue());
            } else if (ratingObj instanceof Long) {
                item.setRating(((Long) ratingObj).floatValue());
            } else if (ratingObj instanceof String) {
                try {
                    item.setRating(Float.parseFloat((String) ratingObj));
                } catch (NumberFormatException e) {
                    item.setRating(0.0f); // 默认值
                }
            } else {
                item.setRating(0.0f); // 默认值
            }
        } else {
            item.setRating(0.0f); // 默认值
        }
        
        // 设置价格 - 添加货币符号
        if (data.containsKey("price")) {
            Object priceObj = data.get("price");
            if (priceObj instanceof Double) {
                item.setPrice("¥" + priceObj);
            } else if (priceObj instanceof Integer) {
                item.setPrice("¥" + priceObj);
            } else if (priceObj instanceof Long) {
                item.setPrice("¥" + priceObj);
            } else if (priceObj instanceof String) {
                if (!((String) priceObj).startsWith("¥")) {
                    item.setPrice("¥" + priceObj);
                } else {
                    item.setPrice((String) priceObj);
                }
            } else {
                item.setPrice("¥0"); // 默认值
            }
        } else {
            item.setPrice("¥0"); // 默认值
        }
        
        // 设置标签 - 数据库中是单个tag字段
        if (data.containsKey("tag")) {
            String tag = (String) data.get("tag");
            if (!TextUtils.isEmpty(tag)) {
                // 如果tag包含逗号，则分割为多个标签
                if (tag.contains(",")) {
                    String[] tags = tag.split(",");
                    for (String t : tags) {
                        item.getTags().add(t.trim());
                    }
                } else {
                    item.getTags().add(tag);
                }
            }
        }
        
        // 设置图片URL - 使用正确的字段名
        if (data.containsKey("img_url")) {
            item.setImageUrl((String) data.get("img_url"));
        }
        
        // 添加读取content字段
        if (data.containsKey("content")) {
            item.setContent((String) data.get("content"));
        }
        
        // 设置位置信息 - 直接从location字段读取
        if (data.containsKey("location")) {
            item.setLocation((String) data.get("location"));
            Log.d(TAG, "读取到位置信息: " + item.getTitle() + " -> " + item.getLocation());
        } else {
            Log.w(TAG, "数据中没有location字段: " + item.getTitle());
        }
        
        return item;
    }
} 