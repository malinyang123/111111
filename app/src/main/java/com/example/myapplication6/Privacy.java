package com.example.myapplication6;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Privacy extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private ImageView mB_home;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件，确保 View 被正确加载
        setContentView(R.layout.activity_privacy);

        // 调整 EdgeToEdge 显示模式
        EdgeToEdge.enable(this);

        // 处理 Android API 30+ 的全屏模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // 确保 main 布局已经被正确加载
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {

        }

        // 设置底部导航栏的点击事件
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Privacy.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent settingIntent = new Intent(Privacy.this, Setting.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent contactsIntent = new Intent(Privacy.this, Contacts.class);
                startActivity(contactsIntent);
            } else if (item.getItemId() == R.id.Profile) {
                Intent libraryIntent = new Intent(Privacy.this, Profile.class);
                startActivity(libraryIntent);
            }
            return true;
        });

        // 返回按钮点击事件
        mB_home = findViewById(R.id.back_button);
        mB_home.setOnClickListener(view -> {
            Intent intent = new Intent(Privacy.this, Setting.class);
            startActivity(intent);
        });
    }


}