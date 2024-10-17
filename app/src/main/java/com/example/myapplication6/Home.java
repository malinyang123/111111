package com.example.myapplication6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.os.Build;

import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {
    private static final String PREFS_NAME = "AccessibilityPrefs";
    private static final String HIGH_CONTRAST_KEY = "high_contrast_mode";  // 键值用于存储高对比度模式状态
    private static final String BACKGROUND_COLOR_KEY = "background_color"; // 键值用于存储背景颜色
    private static final String COLORBLIND_MODE_KEY = "colorblind_mode";  // 键值用于存储色盲模式
    private String[] colorblindModes = {"None", "Red-Green Blind", "Blue-Yellow Blind", "Full Color Blind"}; // 色盲模式列表
    BottomNavigationView bottomNavigationView;
    //监听生命周期 csr加
    String Tag = "my_tag";
    //跳转到help页 csr加
    private ImageView mB_help;
    //跳转到notification页 csr加
    private ImageView mB_notification;
    //跳转到tips页 csr加
    private ImageView mB_maps;
    //跳转到profile页 csr加
    private ImageView mB_profile;
    //emergency call sxl加
    private ImageView mB_search;
    private ImageView emergency_call;
    private ImageView mB_accessibility;
    private ImageView mB_tips;
    private DrawerLayout drawerLayout;  // DrawerLayout 引用
    private ImageView folderIcon;       // Folder 图标引用
    private ImageView contact_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        // 检查高对比度模式状态并应用字体加粗和增大效果
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isHighContrastEnabled = preferences.getBoolean(HIGH_CONTRAST_KEY, false);
        boolean isBackgroundBlack = preferences.getBoolean(BACKGROUND_COLOR_KEY, false); // 检查背景颜色状态
        int colorblindMode = preferences.getInt(COLORBLIND_MODE_KEY, 0); // 获取存储的色盲模式

        applyHighContrastMode(isHighContrastEnabled, isBackgroundBlack);
        applyColorblindMode(colorblindMode);  // 应用色盲模式

        // 全屏设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(Tag, "onCreate: ");

        // 跳转逻辑
        mB_help = findViewById(R.id.Emergency_Contact);
        mB_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Contacts.class);
                startActivity(intent);
            }
        });



        mB_notification = findViewById(R.id.Alert_Notification);
        mB_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, AlertNotification.class);
                startActivity(intent);
            }
        });

        mB_maps = findViewById(R.id.Flood_Sensor_Map);
        mB_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Map.class);
                startActivity(intent);
            }
        });





        mB_tips = findViewById(R.id.Survival_Tips);
        mB_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, SurvivalTips.class);
                startActivity(intent);
            }
        });



        mB_accessibility = findViewById(R.id.imageView17);
        mB_accessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Accessibility.class);
                startActivity(intent);
            }
        });
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Home.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent settingIntent = new Intent(Home.this, Setting.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent contactsIntent = new Intent(Home.this,  Contacts.class);
                startActivity(contactsIntent);
            }
            return true;
        });



        // 跳转手机拨号页面，自动输入000求救号码
        emergency_call = findViewById(R.id.Blue_Background);
        emergency_call.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String phone_num = "000";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                Uri uri = Uri.parse("tel:" + phone_num);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    // 应用高对比度模式和背景颜色
    private void applyHighContrastMode(boolean enable, boolean isBlackBackground) {
        View rootView = findViewById(R.id.main);

        if (isBlackBackground) {
            rootView.setBackgroundColor(Color.BLACK);  // 设置背景为黑色
        } else {
            rootView.setBackgroundColor(Color.WHITE);  // 设置背景为白色
        }


    }

    // 应用色盲模式
    private void applyColorblindMode(int mode) {
        View rootView = findViewById(R.id.main);

        switch (mode) {
            case 0: // 无色盲模式
                resetColorsToDefault(rootView);
                break;
            case 1: // 红绿色盲模式
                setColorsForRedGreenBlind(rootView);
                break;
            case 2: // 蓝黄色盲模式
                setColorsForBlueYellowBlind(rootView);
                break;
            case 3: // 全色盲模式
                setColorsForFullColorBlind(rootView);
                break;
        }
    }

    // 红绿色盲模式颜色设置
    private void setColorsForRedGreenBlind(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(Color.BLUE); // 将红色和绿色替换为蓝色等高对比色
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setColorsForRedGreenBlind(child);
            }
        }
    }

    // 蓝黄色盲模式颜色设置
    private void setColorsForBlueYellowBlind(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(Color.RED); // 将蓝色和黄色替换为红色等高对比色
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setColorsForBlueYellowBlind(child);
            }
        }
    }

    // 全色盲模式颜色设置
    private void setColorsForFullColorBlind(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(Color.BLACK); // 使用黑白对比色
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setColorsForFullColorBlind(child);
            }
        }
    }

    // 恢复默认颜色
    private void resetColorsToDefault(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(Color.BLACK); // 恢复默认颜色
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                resetColorsToDefault(child);
            }
        }
    }

    // 遍历所有视图并应用字体样式
    private void traverseViewsAndApplyStyle(View view, boolean enable) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (enable) {
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextSize(24);  // 字体大小为 24sp
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setTextSize(18);  // 恢复默认大小
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                traverseViewsAndApplyStyle(child, enable);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Tag, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Tag, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Tag, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Tag, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Tag, "onDestroy: ");
    }
}
