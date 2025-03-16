package com.example.tastylog;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.osmdroid.config.Configuration;

import com.example.tastylog.config.Config;

public class TastyLogApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this;
        
        // 初始化Osmdroid配置 - 使用应用自己的SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE);
        Configuration.getInstance().load(context, prefs);
        
        // 设置用户代理，避免被服务器拒绝
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }
} 