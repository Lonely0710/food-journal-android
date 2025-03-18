package com.example.tastylog;

import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Date;

import io.appwrite.models.Session;
import io.appwrite.models.User;
import io.appwrite.models.Document;
import com.example.tastylog.config.AppConfig;

/**
 * Java 包装类，访问 Kotlin 的 Appwrite 对象
 */
public class AppwriteWrapper {
    private static final String TAG = "AppwriteWrapper";
    private static AppwriteWrapper instance;
    
    // 私有构造函数，防止外部实例化
    private AppwriteWrapper() {
    }
    
    // 单例模式获取实例
    public static synchronized AppwriteWrapper getInstance() {
        if (instance == null) {
            instance = new AppwriteWrapper();
        }
        return instance;
    }
    
    /**
     * 初始化 Appwrite 客户端
     * @param context 应用上下文
     */
    public static void init(Context context) {
        try {
            // 直接调用 Appwrite 的 init 方法
            Appwrite.INSTANCE.init(context);
            Log.d(TAG, "Appwrite 初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Appwrite 初始化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return CompletableFuture<Session> 登录会话
     */
    public static CompletableFuture<Session> login(String email, String password) {
        CompletableFuture<Session> future = new CompletableFuture<>();
        
        try {
            // 使用回调版本的登录方法
            Appwrite.INSTANCE.loginWithCallback(
                email, 
                password,
                session -> {
                    future.complete(session);
                    return null;
                },
                error -> {
                    Log.e(TAG, "登录失败: " + error.getMessage(), error);
                    future.completeExceptionally(error);
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e(TAG, "调用登录方法失败: " + e.getMessage(), e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 用户注册
     * @param email 邮箱
     * @param password 密码
     * @param name 用户名
     * @return CompletableFuture<User> 用户信息
     */
    public static CompletableFuture<User<Map<String, Object>>> register(String email, String password, String name) {
        CompletableFuture<User<Map<String, Object>>> future = new CompletableFuture<>();
        
        try {
            // 使用回调版本的注册方法
            Appwrite.INSTANCE.registerWithCallback(
                email,
                password,
                name,
                user -> {
                    future.complete(user);
                    return null;
                },
                error -> {
                    Log.e(TAG, "注册失败: " + error.getMessage(), error);
                    future.completeExceptionally(error);
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e(TAG, "调用注册方法失败: " + e.getMessage(), e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * 用户登出
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    public static void logout(Runnable onSuccess, Consumer<Exception> onError) {
        try {
            // 使用回调版本的登出方法
            Appwrite.INSTANCE.logoutWithCallback(
                () -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    return null;
                },
                error -> {
                    if (onError != null) {
                        onError.accept(error);
                    }
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e(TAG, "调用登出方法失败: " + e.getMessage(), e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    /**
     * 获取用户食物列表
     * @param userId
     * @param onSuccess
     * @param onError
     */
    public void getUserFoodItems(
        String userId,
        Consumer<java.util.List<Document<Map<String, Object>>>> onSuccess,
        Consumer<Exception> onError
    ) {
        Appwrite.INSTANCE.getUserFoodItemsWithCallback(
            userId,
            documents -> {
                onSuccess.accept(documents);
                return null;
            },
            error -> {
                onError.accept(error);
                return null;
            }
        );
    }

    /**
     * 上传文件
     * @param bucketId
     * @param fileName
     * @param fileBytes
     * @param onSuccess
     * @param onError
     */
    public void uploadFile(
        String bucketId,
        String fileName,
        byte[] fileBytes,
        Consumer<String> onSuccess,
        Consumer<Exception> onError
    ) {
        Appwrite.INSTANCE.uploadFileWithCallback(
            bucketId,
            fileName,
            fileBytes,
            fileId -> {
                onSuccess.accept(fileId);
                return null;
            },
            error -> {
                onError.accept(error);
                return null;
            }
        );
    }
    
    // 获取文件预览URL
    public String getFilePreviewUrl(String bucketId, String fileId) {
        return Appwrite.INSTANCE.getFilePreviewUrl(bucketId, fileId);
    }
    
    // 获取当前登录用户ID
    public String getCurrentUserId() {
        return Appwrite.INSTANCE.getCurrentUserId();
    }

    /**
     * 添加食物记录
     */
    public void addFoodItem(
        String userId,
        String title,
        String time,
        String imgUrl,
        float rating,
        double price,
        String tag,
        String content,
        String location,
        Consumer<Document<Map<String, Object>>> onSuccess,
        Consumer<Exception> onError
    ) {
        Appwrite.INSTANCE.addFoodItemWithCallback(
            userId,
            title,
            time,
            imgUrl,
            (double) rating,
            price,
            tag,
            content,  // 直接传递原始content
            location, // 直接传递location
            document -> {
                onSuccess.accept(document);
                return null;
            },
            error -> {
                onError.accept(error);
                return null;
            }
        );
    }

    // 添加配置验证
    private void validateConfig() {
        if (AppConfig.PROJECT_ID.equals("your_project_id_here") ||
            AppConfig.DATABASE_ID.equals("your_database_id_here")) {
            throw new IllegalStateException("请先配置AppConfig.java文件！");
        }
    }

    /**
     * 删除食物记录
     */
    public void deleteFoodItem(
        String userId,  // 保留参数但不传递给 Kotlin 方法
        String foodId,
        Runnable onSuccess,
        Consumer<Exception> onError
    ) {
        Log.d(TAG, "正在调用删除方法, foodId=" + foodId);
        
        // 检查 foodId 是否为空
        if (foodId == null || foodId.isEmpty()) {
            Log.e(TAG, "删除失败: foodId 为空");
            if (onError != null) {
                onError.accept(new IllegalArgumentException("美食记录ID不能为空"));
            }
            return;
        }
        
        try {
            Appwrite.INSTANCE.deleteFoodItemWithCallback(
                foodId,  // 只传递 foodId
                () -> {
                    Log.d(TAG, "删除成功, foodId=" + foodId);
                    onSuccess.run();
                    return null;
                },
                error -> {
                    Log.e(TAG, "删除失败: " + error.getMessage(), error);
                    onError.accept(error);
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e(TAG, "调用删除方法异常: " + e.getMessage(), e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    /**
     * 更新食物记录到Appwrite数据库
     */
    public void updateFoodItem(
        String userId,
        String documentId,
        String title,
        String time,
        String imageUrl,
        float rating,
        double price,
        String tags,
        String notes,
        String location,
        Consumer<Document> onSuccess,
        Consumer<Exception> onError) {
        
        Log.d(TAG, "正在调用更新方法, documentId=" + documentId);
        
        Appwrite.INSTANCE.updateFoodItemWithCallback(
            userId,
            documentId,
            title,
            time,
            imageUrl,
            (double) rating,
            price,
            tags,
            notes,
            location,
            document -> {
                Log.d(TAG, "更新成功, documentId=" + documentId);
                onSuccess.accept(document);
                return null;
            },
            error -> {
                Log.e(TAG, "更新失败: " + error.getMessage(), error);
                onError.accept(error);
                return null;
            }
        );
    }
}