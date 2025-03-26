package com.example.tastylog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * 用户登录界面
 * 提供邮箱密码登录和注册功能
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvToRegister;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        
        setContentView(R.layout.activity_login);
        
        // 初始化视图
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvToRegister = findViewById(R.id.tv_to_register);
        
        // 添加进度指示器
        progressIndicator = findViewById(R.id.progress_indicator);
        if (progressIndicator != null) {
            // 即使从布局中找到了指示器，也要设置颜色
            progressIndicator.setIndicatorColor(getResources().getColor(R.color.orange_500));
        } else {
            // 如果布局中没有进度指示器，可以在代码中创建
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
            progressIndicator.setIndicatorColor(getResources().getColor(R.color.orange_500));
            progressIndicator.setVisibility(View.GONE);
        }
        
        // 初始化 Appwrite
        AppwriteWrapper.init(getApplicationContext());
        
        // 设置登录按钮点击事件
        btnLogin.setOnClickListener(v -> login());
        
        // 设置注册文本点击事件
        tvToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
    
    private void login() {
        // 获取输入
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }
        
        // 显示加载中
        setLoading(true);
        
        // 调用登录方法
        AppwriteWrapper.login(email, password)
            .thenAccept(session -> {
                // 登录成功
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    
                    // 跳转到主页
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out);
                    finish();
                });
            })
            .exceptionally(e -> {
                // 登录失败
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "登录失败", e);
                });
                return null;
            });
    }
    
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
            progressIndicator.setVisibility(View.GONE);
        }
    }
}