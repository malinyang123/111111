package com.example.myapplication6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class Setting extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    // 日志标签
    private static final String TAG = "ProfileNotLog";
    private TextView text_private;
    // UI 元素
    private ImageView mB_profile;
    private ImageView contact_button;
    private TextView text_login;
    private TextView btnLoginLogout;
    private TextView text_username;

    // SharedPreferences 用于存储登录状态
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 设置全屏并隐藏系统导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                // 隐藏导航栏
                controller.hide(WindowInsets.Type.navigationBars());
                // 设置系统栏行为，允许通过滑动显示导航栏
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // 对于 API 30 以下的版本，使用旧的沉浸模式实现
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // 处理窗口内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        text_private = findViewById(R.id.privacy);
        text_private .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, Privacy.class);
                startActivity(intent);
            }
        });
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Setting.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent settingIntent = new Intent(Setting.this, Contacts.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent contactsIntent = new Intent(Setting.this,  Contacts.class);
                startActivity(contactsIntent);
            } else if (item.getItemId() == R.id.Profile) {
                Intent libraryIntent = new Intent(Setting.this,  Profile.class);
                startActivity(libraryIntent);
            }
            return true;
        });
        // 初始化返回按钮

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        // 初始化登录/登出按钮
        btnLoginLogout = findViewById(R.id.logout);

        // 根据登录状态更新按钮
        updateButton();

        // 设置按钮点击事件
        btnLoginLogout.setOnClickListener(v -> {
            if (isLoggedIn()) {
                // 已登录，执行登出操作
                logout();
            } else {
                // 未登录，跳转到登录界面
                Intent intent = new Intent(Setting.this, login_activity.class);
                startActivity(intent);
            }
        });


        // 如果已登录但 currentUser 为 null，尝试初始化 currentUser
        if (isLoggedIn() && login_activity.currentUser == null) {
            String username = sharedPreferences.getString(KEY_USERNAME, null);
            if (username != null) {
                retrieveCurrentUser(username);
            }
        }
    }

    // 检查是否已登录
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 更新按钮的显示内容
    private void updateButton() {
        text_login = findViewById(R.id.logout);
        //text_username = findViewById(R.id.not_logged_in);
        if (isLoggedIn()) {
            if (login_activity.currentUser != null) {
                String username = login_activity.currentUser.username;
                text_login.setText(R.string.logout);
                //text_username.setText(username);
            } else {
                // 尝试从 SharedPreferences 中获取用户名
                String username = sharedPreferences.getString(KEY_USERNAME, "User");
                text_login.setText(R.string.logout);
               // text_username.setText(username);
                // 您也可以在此处重新初始化 currentUser
                retrieveCurrentUser(username);
            }
        } else {
            text_login.setText(R.string.login);
            //text_username.setText(R.string.not_logged_in);
        }
    }

    // 登出操作
    private void logout() {
        // 清除登录状态
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // 清除当前用户信息
        login_activity.currentUser = null;

        // 更新按钮显示
        updateButton();

        // 可选择显示提示信息
        // Toast.makeText(ProfileNotLog.this, "Logged out 已登出", Toast.LENGTH_SHORT).show();
    }

    // 从数据库检索 currentUser
    private void retrieveCurrentUser(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    login_activity.currentUser = user;
                    // 更新用户名显示

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Setting.this, getString(R.string.database_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButton();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}