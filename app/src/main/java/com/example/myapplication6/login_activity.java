package com.example.myapplication6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences; // 导入 SharedPreferences
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import android.text.Editable;
import android.text.TextWatcher;

public class login_activity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    private DatabaseReference databaseReference;

    // 全局的用户对象
    public static User currentUser;

    // SharedPreferences 用于存储登录状态
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 设置全屏并隐藏系统导航栏
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                // 隐藏导航栏
                controller.hide(WindowInsets.Type.navigationBars());
                // 设置系统栏行为，允许通过滑动显示导航栏
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // 对于API 30以下的版本，使用旧的沉浸模式实现
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 检查登录状态
        checkLoginStatus();

        // 初始化视图
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton      = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        // 初始化 Firebase 数据库引用
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // 添加文本变化监听器，启用登录按钮
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 忽略
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 忽略
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkInputFields();
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            // 调用登录方法
            login(username, password);
        });


        // 返回按钮点击事件
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // 结束当前活动，返回上一界面
    }

    // 新增方法：检查用户登录状态
    private void checkLoginStatus() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            // 用户已登录，获取用户名
            String username = sharedPreferences.getString(KEY_USERNAME, null);
            if (username != null) {
                // 从数据库获取用户信息
                databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            currentUser = user;
                            // 跳转到主界面
                            Intent intent = new Intent(login_activity.this, Setting.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 用户数据不存在，清除登录状态
                            clearLoginStatus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(login_activity.this, "Database Fail! " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 用户名为空，清除登录状态
                clearLoginStatus();
            }
        }
        // 如果未登录，什么都不做，显示登录界面
    }

    private void clearLoginStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void checkInputFields() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // 检查用户名是否为10位数字
        boolean isUsernameValid = username.matches("\\d{10}");

        // 启用登录按钮的条件
        loginButton.setEnabled(!username.isEmpty() && !password.isEmpty() && isUsernameValid);

        if (!isUsernameValid && !username.isEmpty()) {
            usernameEditText.setError(getString(R.string.invalid_phone_number));
        } else {
            usernameEditText.setError(null); // 清除错误信息
        }
    }

    private void login(String username, String password) {
        // 再次验证用户名是否为10位数字
        if (!username.matches("\\d{10}")) {
            loadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(login_activity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查用户名是否存在
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProgressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    // 用户名存在，验证密码
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.password.equals(password)) {
                        // 密码正确，登录成功
                        Toast.makeText(login_activity.this, R.string.welcome, Toast.LENGTH_SHORT).show();
                        // 保存当前用户信息
                        currentUser = user;
                        // 保存登录状态
                        saveLoginStatus(username);
                        // 跳转到主界面
                        Intent intent = new Intent(login_activity.this, Setting.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 密码错误
                        Toast.makeText(login_activity.this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 用户名不存在，自动注册
                    User newUser = new User(username, password);
                    databaseReference.child(username).setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(login_activity.this, R.string.welcome, Toast.LENGTH_SHORT).show();
                                // 保存当前用户信息
                                currentUser = newUser;
                                // 保存登录状态
                                saveLoginStatus(username);
                                // 跳转到主界面
                                Intent intent = new Intent(login_activity.this, Setting.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(login_activity.this, getString(R.string.login_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(login_activity.this, "Database Fail! " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 新增方法：保存登录状态
    private void saveLoginStatus(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
}