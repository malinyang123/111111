package com.example.myapplication6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Accessibility extends AppCompatActivity {
    private static final String PREFS_NAME = "AccessibilityPrefs";
    private static final String HIGH_CONTRAST_KEY = "high_contrast_mode";  // 键值用于存储高对比度模式状态
    private static final String BACKGROUND_COLOR_KEY = "background_color"; // 键值用于存储背景颜色状态
    private static final String COLORBLIND_MODE_KEY = "colorblind_mode"; // 键值用于存储色盲模式
    private ImageView mB_home;
    private boolean isHighContrastModeEnabled = false;  // 追踪高对比度模式是否启用
    private boolean isBackgroundBlack = false;  // 用于追踪背景颜色是否为黑色
    private Spinner colorblindModeSpinner;
    private String[] colorblindModes = {"None", "Red-Green Blind", "Blue-Yellow Blind", "Full Color Blind"};
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_accessibility);


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Accessibility.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent settingIntent = new Intent(Accessibility.this, Setting.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent contactsIntent = new Intent(Accessibility.this,  Contacts.class);
                startActivity(contactsIntent);
            }
            return true;
        });


        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isHighContrastModeEnabled = preferences.getBoolean(HIGH_CONTRAST_KEY, false);
        isBackgroundBlack = preferences.getBoolean(BACKGROUND_COLOR_KEY, false);
        int savedColorblindMode = preferences.getInt(COLORBLIND_MODE_KEY, 0); // 获取存储的色盲模式

        // 设置高对比度开关
        Switch highContrastSwitch = findViewById(R.id.high_contrast_mode_switch1);
        highContrastSwitch.setChecked(isHighContrastModeEnabled);  // 根据状态设置开关
        highContrastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighContrastModeEnabled = isChecked;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(HIGH_CONTRAST_KEY, isChecked);
            editor.apply();
            // 启用或禁用高对比度模式
            applyHighContrastMode(isChecked);
        });

        // 设置背景颜色开关
        Switch colorFilteringSwitch = findViewById(R.id.color_filtering_switch2);
        colorFilteringSwitch.setChecked(isBackgroundBlack); // 根据存储的背景颜色状态设置
        colorFilteringSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBackgroundBlack = isChecked;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(BACKGROUND_COLOR_KEY, isChecked);
            editor.apply();
            // 启用或禁用黑色背景
            applyBackgroundColor(isChecked);
        });

        // 设置色盲模式选择框
        colorblindModeSpinner = findViewById(R.id.colorblind_mode_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorblindModes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorblindModeSpinner.setAdapter(adapter);

        // 恢复之前的选择
        colorblindModeSpinner.setSelection(savedColorblindMode);

        colorblindModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 存储用户选择的色盲模式
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(COLORBLIND_MODE_KEY, position);
                editor.apply();
                applyColorblindMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        mB_home = findViewById(R.id.back_button);
        mB_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Accessibility.this, Home.class);
                startActivity(intent);
            }
        });


        // 根据存储的状态应用高对比度模式、背景颜色和色盲模式
        //applyHighContrastMode(isHighContrastModeEnabled);
        //applyBackgroundColor(isBackgroundBlack);
        //applyColorblindMode(savedColorblindMode);  // 应用色盲模式
    }

    // 启用或禁用高对比度模式
    private void applyHighContrastMode(boolean enable) {
        View rootView = findViewById(R.id.main);
        traverseViewsAndApplyStyle(rootView, enable);
    }

    // 遍历所有 TextView 并设置字体样式
    private void traverseViewsAndApplyStyle(View view, boolean enable) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (enable) {
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24); // 设置字体大小为 24sp
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // 恢复默认大小
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

    // 启用或禁用黑色背景
    private void applyBackgroundColor(boolean isBlack) {
        View rootView = findViewById(R.id.main);
        if (isBlack) {
            rootView.setBackgroundColor(Color.BLACK);  // 设置背景为黑色
        } else {
            rootView.setBackgroundColor(Color.WHITE);  // 设置背景为白色（默认）
        }
    }

    // 根据选择的色盲模式调整应用颜色
    private void applyColorblindMode(int mode) {
        View rootView = findViewById(R.id.main);

        switch (mode) {
            case 0: // None (没有色盲模式)
                resetColorsToDefault(rootView);
                break;
            case 1: // Red-Green Blind
                setColorsForRedGreenBlind(rootView);
                break;
            case 2: // Blue-Yellow Blind
                setColorsForBlueYellowBlind(rootView);
                break;
            case 3: // Full Color Blind
                setColorsForFullColorBlind(rootView);
                break;
        }
    }

    // 为红绿色盲调整颜色
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

    // 为蓝黄色盲调整颜色
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

    // 为全色盲调整颜色
    private void setColorsForFullColorBlind(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(Color.GRAY); // 使用灰色
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
}
