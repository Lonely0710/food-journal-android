package com.example.tastylog;

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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvToLogin;
    private CircularProgressIndicator progressIndicator;
    private View loadingOverlay;

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
        
        setContentView(R.layout.activity_register);
        
        // 初始化视图
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvToLogin = findViewById(R.id.tv_to_login);
        
        // 添加进度指示器
        progressIndicator = findViewById(R.id.progress_indicator);
        if (progressIndicator == null) {
            // 如果布局中没有进度指示器，可以在代码中创建
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
            progressIndicator.setVisibility(View.GONE);
        }
        
        // 初始化控件
        loadingOverlay = findViewById(R.id.loading_overlay);
        
        // 设置注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            // 获取输入
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            // 验证输入
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 显示加载中
            setLoading(true);
            
            // 在这里添加日志确认调用
            Log.d(TAG, "准备调用注册方法，用户名: " + name + ", 邮箱: " + email);
            
            // 调用注册方法
            AppwriteWrapper.register(email, password, name)
                .thenAccept(user -> {
                    // 注册成功
                    Log.d(TAG, "注册成功，用户ID: " + user.getId());
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        
                        // 跳转到登录页
                        finish();
                    });
                })
                .exceptionally(e -> {
                    // 注册失败
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "注册失败", e);
                    });
                    return null;
                });
        });
        
        // 设置登录文本点击事件
        tvToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }
    
    private void setLoading(boolean isLoading) {
        loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // 禁用或启用输入控件
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        etName.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        tvToLogin.setEnabled(!isLoading);
    }
}