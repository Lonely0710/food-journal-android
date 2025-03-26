package com.example.tastylog.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.tastylog.AppwriteWrapper;
import com.example.tastylog.model.FoodItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.appwrite.models.Document;

/**
 * 食物数据仓库类
 * 
 * 负责管理食物记录的数据访问
 */
public class FoodRepository {
    private static final String TAG = "FoodRepository";
    private static FoodRepository instance;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final List<FoodItem> cachedFoodItems = new ArrayList<>();
    private final AppwriteWrapper appwrite;

    private FoodRepository(Context context) {
        appwrite = AppwriteWrapper.getInstance();
    }

    /**
     * 获取FoodRepository单例实例
     *
     * @param context 应用上下文
     * @return FoodRepository实例
     */
    public static synchronized FoodRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FoodRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 食物列表数据回调接口
     */
    public interface FoodListCallback {
        void onFoodListLoaded(List<FoodItem> foodItems);
        void onError(Exception e);
    }

    /**
     * 单个食物记录回调接口
     */
    public interface FoodItemCallback {
        void onSuccess(FoodItem foodItem);
        void onError(Exception e);
    }

    /**
     * 获取所有食物记录
     * 从Appwrite云端获取当前用户的所有食物记录
     *
     * @param callback 数据加载回调
     */
    public void getAllFoodItems(FoodListCallback callback) {
        executor.execute(() -> {
            try {
                // 获取当前用户ID
                String userId = AppwriteWrapper.getInstance().getCurrentUserId();
                
                // 如果没有用户ID,返回空列表
                if (TextUtils.isEmpty(userId)) {
                    callback.onFoodListLoaded(new ArrayList<>());
                    return;
                }
                
                // 从Appwrite获取数据
                fetchFoodItemsFromAppwrite(userId, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error loading food items", e);
                callback.onError(e);
            }
        });
    }

    /**
     * 添加新的食物记录
     *
     * @param foodItem 要添加的食物记录
     * @param callback 添加结果回调
     */
    public void addFoodItem(FoodItem foodItem, FoodItemCallback callback) {
        executor.execute(() -> {
            try {
                // 转换为JSON格式
                JSONObject json = foodItemToJson(foodItem);
                
                // 更新本地缓存
                cachedFoodItems.add(foodItem);
                
                callback.onSuccess(foodItem);
            } catch (Exception e) {
                Log.e(TAG, "Error adding food item", e);
                callback.onError(e);
            }
        });
    }

    /**
     * 将FoodItem对象转换为JSON格式
     * 转换后的JSON结构匹配Appwrite数据库要求
     *
     * @param foodItem 要转换的食物记录
     * @return 转换后的JSON对象
     * @throws JSONException JSON转换异常
     */
    private JSONObject foodItemToJson(FoodItem foodItem) throws JSONException {
        JSONObject json = new JSONObject();
        
        // 设置基本字段
        json.put("food_id", foodItem.getId());
        json.put("title", foodItem.getTitle());
        json.put("time", foodItem.getTime());
        json.put("rating", foodItem.getRating());
        
        // 处理价格字段 - 移除货币符号并转换为数值
        try {
            String priceStr = foodItem.getPrice().replaceAll("[^0-9.]", "");
            double priceValue = Double.parseDouble(priceStr);
            json.put("price", priceValue);
        } catch (NumberFormatException e) {
            json.put("price", foodItem.getPrice());
        }
        
        // 处理标签 - 选择第一个标签或连接所有标签
        List<String> tags = foodItem.getTags();
        if (tags != null && !tags.isEmpty()) {
            json.put("tag", tags.get(0));
        } else {
            json.put("tag", "");
        }
        
        // 设置图片URL和用户ID
        json.put("img_url", foodItem.getImageUrl());
        json.put("user_id", AppwriteWrapper.getInstance().getCurrentUserId());
        
        return json;
    }

    /**
     * 从Appwrite获取用户的食物记录
     *
     * @param userId 用户ID
     * @param callback 数据加载回调
     */
    private void fetchFoodItemsFromAppwrite(String userId, FoodListCallback callback) {
        AppwriteWrapper.getInstance().getUserFoodItems(
            userId,
            documents -> {
                try {
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
                        
                        // 转换为FoodItem对象
                        FoodItem item = documentToFoodItem(document);
                        foodItems.add(item);
                        
                        // 添加转换后的日志
                        Log.d(TAG, "Converted FoodItem: " + item.getTitle() + ", Rating: " + item.getRating() + ", Price: " + item.getPrice());
                    }
                    
                    // 更新缓存并回调
                    cachedFoodItems.clear();
                    cachedFoodItems.addAll(foodItems);
                    callback.onFoodListLoaded(foodItems);
                } catch (Exception e) {
                    Log.e(TAG, "处理Appwrite响应时发生错误", e);
                    callback.onError(e);
                }
            },
            error -> {
                Log.e(TAG, "Error fetching food items from Appwrite", error);
                callback.onError(error);
            }
        );
    }

    /**
     * 将Appwrite文档转换为FoodItem对象
     *
     * @param document Appwrite文档对象
     * @return 转换后的FoodItem对象
     */
    private FoodItem documentToFoodItem(Document<Map<String, Object>> document) {
        FoodItem item = new FoodItem();
        Map<String, Object> data = document.getData();
        
        // 设置文档ID和食物ID
        item.setDocumentId(document.getId());
        
        // 设置food_id - 存在时使用，不存在时才用document.getId()
        if (data.containsKey("food_id")) {
            item.setId((String) data.get("food_id"));
        } else {
            item.setId(document.getId());
        }
        
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