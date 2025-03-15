package com.example.tastylog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setAnimation(R.raw.cooking_loading);
        animationView.playAnimation();

        // 2秒后跳转到登录页
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                 finish();
                //             // 检查用户是否已登录
                // String userId = AppwriteWrapper.getInstance().getCurrentUserId();
                // if (userId != null && !userId.isEmpty()) {
                //     // 已登录，直接进入主页
                //     startActivity(new Intent(SplashActivity.this, MainActivity.class));
                // } else {
                //     // 未登录，进入登录页
                //     startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                // }
                // finish(); // 结束当前Activity
            }
        }, 2000);
    }
} 