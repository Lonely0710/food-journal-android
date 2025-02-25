package com.example.tastylog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etVerifyCode;
    private Button btnGetCode;
    private Button btnLogin;
    private ImageView ivWechat;
    private ImageView ivAlipay;
    private ImageView ivGoogle;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etVerifyCode = findViewById(R.id.et_verify_code);
        btnGetCode = findViewById(R.id.btn_get_code);
        btnLogin = findViewById(R.id.btn_login);
        ivWechat = findViewById(R.id.iv_wechat);
        ivAlipay = findViewById(R.id.iv_alipay);
        ivGoogle = findViewById(R.id.iv_google);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            // 暂时不做验证,直接跳转到主页
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        btnGetCode.setOnClickListener(v -> {
            // TODO: 实现获取验证码逻辑
        });

        // 社交登录图标点击动画
        View.OnClickListener socialLoginListener = v -> {
            // 加载scale_up动画资源并应用到被点击的视图上
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
            // TODO: 实现社交登录逻辑
        };

        // 将动画应用到三个社交登录图标
        ivWechat.setOnClickListener(socialLoginListener);
        ivAlipay.setOnClickListener(socialLoginListener);
        ivGoogle.setOnClickListener(socialLoginListener);

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: 实现忘记密码逻辑
        });

        tvRegister.setOnClickListener(v -> {
            // TODO: 实现注册逻辑
        });
    }
} 