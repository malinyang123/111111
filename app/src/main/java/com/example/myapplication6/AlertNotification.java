package com.example.myapplication6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertNotification extends AppCompatActivity {
    //监听生命周期 csr加
    String Tag="my_tag";
    //跳转到Home页 csr加
    private ImageView mB_home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert_notification);
        // 获取从 Map.java 传递的数据
        Intent intent = getIntent();
        float precipitation = intent.getFloatExtra("precipitation", 0);
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        long timestamp = intent.getLongExtra("timestamp", 0);

        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date(timestamp));

        // 显示降雨量、经纬度和时间
        TextView alertTextView = findViewById(R.id.alertTextView);
        alertTextView.setText(String.format(Locale.getDefault(),
                "Flood Risk Alert!\nRainfall: %.2f mm\nLatitude: %.5f\nLongitude: %.5f\nTime: %s",
                precipitation, latitude, longitude, formattedDate));
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(Tag, "onCreate: ");

        mB_home = findViewById(R.id.imageView5);
        mB_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlertNotification.this, Home.class);
                startActivity(intent);
            }
        });

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