package com.example.myapplication6;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class bottom_sheet_layout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.bottomsheetlayout);

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

        // 获取传递过来的群组成员信息
        List<Map.GroupMemberInfo> groupMembers = (List<Map.GroupMemberInfo>) getIntent().getSerializableExtra("groupMembers");

        // 找到布局中的容器
        LinearLayout groupMemberList = findViewById(R.id.group_member_list);

        // 动态添加群组成员信息
        if (groupMembers != null && !groupMembers.isEmpty()) {
            for (Map.GroupMemberInfo member : groupMembers) {
                // 创建新布局显示群组成员信息
                LinearLayout memberLayout = new LinearLayout(this);
                memberLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameTextView = new TextView(this);
                nameTextView.setText(member.phoneNumber + " - " + member.remark);
                memberLayout.addView(nameTextView);

                groupMemberList.addView(memberLayout);
            }
        } else {
            TextView emptyText = new TextView(this);
            emptyText.setText("No group members found.");
            groupMemberList.addView(emptyText);
        }
    }
}
