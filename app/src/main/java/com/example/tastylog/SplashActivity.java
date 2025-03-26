package com.example.tastylog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

/**
 * 启动页Activity
 * 显示应用加载动画，并根据登录状态决定跳转到登录页或主页
 */
public class SplashActivity extends AppCompatActivity {
    // 启动页显示时间(毫秒)
    private static final int SPLASH_DELAY = 2000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化并播放加载动画
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setAnimation(R.raw.cooking_loading);
        animationView.playAnimation();

        // 延迟后进行页面跳转
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DELAY);
    }
    
    /**
     * 根据登录状态决定跳转到登录页或主页
     */
    private void navigateToNextScreen() {
        String userId = AppwriteWrapper.getInstance().getCurrentUserId();
        Intent intent;
        
        if (userId != null && !userId.isEmpty()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
} 