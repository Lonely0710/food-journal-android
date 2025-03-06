package com.example.tastylog;

import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.appwrite.models.Session;
import io.appwrite.models.User;

/**
 * Java 包装类，访问 Kotlin 的 Appwrite 对象
 */
public class AppwriteWrapper {
    private static final String TAG = "AppwriteWrapper";
    
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
}