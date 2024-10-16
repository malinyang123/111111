package com.example.myapplication6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Contacts extends AppCompatActivity {

    private EditText etAssociatePhone;
    private ImageView btnAssociate;
    private RecyclerView rvGroupMembers;
    private GroupMemberAdapter adapter;
    private List<User> groupMembers;
    String Tag = "my_tag";
    private DatabaseReference usersRef;
    private ImageView mB_profile;
    private ImageView mB_home;
    // 用于存储当前用户的电话号码
    private String currentUserPhone = null;
    BottomNavigationView bottomNavigationView;
    // 新增的视图
    private TextView tvReminder;
    private Button btnGoToLogin;

    // SharedPreferences 用于存储登录状态
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

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

        // 初始化视图
        etAssociatePhone = findViewById(R.id.contact_name_blank);
        btnAssociate = findViewById(R.id.contact_add_button);
        rvGroupMembers = findViewById(R.id.rv_group_members);

        // 新增的视图
        tvReminder = findViewById(R.id.tv_reminder);
        btnGoToLogin = findViewById(R.id.btn_go_to_login);

        // 初始化数据库引用
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (isLoggedIn()) {
            if (login_activity.currentUser == null) {
                String username = sharedPreferences.getString(KEY_USERNAME, null);
                if (username != null) {
                    retrieveCurrentUser(username);
                } else {
                    // 用户名不存在，显示登录提醒
                    showLoginReminder();
                }
            } else {
                // 初始化界面
                initializeUI();
            }
        } else {
            // 用户未登录，显示提醒
            //showLoginReminder();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Contacts.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent settingIntent = new Intent(Contacts.this, Contacts.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent contactsIntent = new Intent(Contacts.this,  Setting.class);
                startActivity(contactsIntent);
            } else if (item.getItemId() == R.id.Profile) {
                Intent libraryIntent = new Intent(Contacts.this,  Profile.class);
                startActivity(libraryIntent);
            }
            return true;
        });

    }

    // 初始化界面
    private void initializeUI() {
        // 用户已登录，正常显示联系人列表
        etAssociatePhone.setVisibility(View.VISIBLE);
        btnAssociate.setVisibility(View.VISIBLE);
        rvGroupMembers.setVisibility(View.VISIBLE);

        tvReminder.setVisibility(View.GONE);
        btnGoToLogin.setVisibility(View.GONE);

        currentUserPhone = login_activity.currentUser.username;

        // 初始化列表
        groupMembers = new ArrayList<>();
        adapter = new GroupMemberAdapter(groupMembers, this::onEditClicked, this::onDeleteClicked);
        rvGroupMembers.setLayoutManager(new LinearLayoutManager(this));
        rvGroupMembers.setAdapter(adapter);

        // 设置添加关联用户按钮的点击事件
        btnAssociate.setOnClickListener(v -> {
            String associatePhone = etAssociatePhone.getText().toString().trim();
            if (associatePhone.isEmpty()) {
                Toast.makeText(Contacts.this, R.string.please_enter_phone_number, Toast.LENGTH_SHORT).show();
            } else if (!associatePhone.matches("\\d{10}")) {
                Toast.makeText(Contacts.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            } else if (associatePhone.equals(currentUserPhone)) {
                Toast.makeText(Contacts.this, R.string.cannot_associate_self, Toast.LENGTH_SHORT).show();
            } else {
                // 输入备注
                showAddRemarkDialog(associatePhone);
            }
        });

        // 显示关联的用户信息
        displayGroupMembers();
    }

    // 检查是否已登录
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 从数据库检索 currentUser
    private void retrieveCurrentUser(String username) {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            // ... 原有代码
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    login_activity.currentUser = user;
                    // 初始化界面
                    initializeUI();
                } else {
                    // 用户不存在，可能已被删除，清除登录状态
                    logout();
                    showLoginReminder();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Contacts.this, getString(R.string.database_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
                showLoginReminder();
            }
        });
    }

    // 显示登录提醒
    private void showLoginReminder() {
        // 用户未登录，显示提醒文字和跳转按钮
        etAssociatePhone.setVisibility(View.GONE);
        btnAssociate.setVisibility(View.GONE);
        rvGroupMembers.setVisibility(View.GONE);

        tvReminder.setVisibility(View.VISIBLE);
        btnGoToLogin.setVisibility(View.VISIBLE);

        btnGoToLogin.setOnClickListener(v -> {
            // 跳转到 Profile 页面（假设您的 Profile 活动为 ProfileNotLog）
            Intent intent = new Intent(Contacts.this, Setting.class);
            startActivity(intent);
        });
    }

    // 登出操作
    private void logout() {
        // 清除登录状态
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // 清除当前用户信息
        login_activity.currentUser = null;
    }

    // 添加备注对话框
    private void showAddRemarkDialog(String associatePhone) {
        final EditText input = new EditText(this);
        input.setHint(R.string.enter_remark);
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_remark)
                .setView(input)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String remark = input.getText().toString().trim();
                    associateUser(associatePhone, remark);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 关联用户的方法
    private void associateUser(String associatePhone, String remark) {
        // 由于已经确认用户已登录，currentUserPhone 不为 null
        // 检查关联的用户是否存在
        usersRef.child(associatePhone).addListenerForSingleValueEvent(new ValueEventListener() {
            // ... 原有代码
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 关联的用户存在，开始关联

                    // 更新当前用户的 groups 节点
                    usersRef.child(currentUserPhone).child("groups").child(associatePhone).setValue(remark);

                    // 更新关联用户的 groups 节点（备注可为空）
                    usersRef.child(associatePhone).child("groups").child(currentUserPhone).setValue("");

                    // 更新本地的 currentUser 对象
                    login_activity.currentUser.addGroupMember(associatePhone, remark);

                    Toast.makeText(Contacts.this, R.string.association_successful, Toast.LENGTH_SHORT).show();

                    // 清空输入框
                    etAssociatePhone.setText("");

                    // 刷新显示关联的用户信息
                    refreshCurrentUserData();

                } else {
                    // 关联的用户不存在
                    Toast.makeText(Contacts.this, R.string.user_not_exist, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Contacts.this, getString(R.string.database_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 显示关联的用户信息
    private void displayGroupMembers() {
        if (login_activity.currentUser == null) {
            Toast.makeText(Contacts.this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> groups = login_activity.currentUser.getGroups();
        if (groups == null || groups.isEmpty()) {
            groupMembers.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(Contacts.this, R.string.no_associated_users, Toast.LENGTH_SHORT).show();
            return;
        }

        groupMembers.clear();

        // 计数器，用于判断何时所有数据加载完成
        final int[] counter = {groups.size()};

        for (String phoneNumber : groups.keySet()) {
            // 获取每个关联用户的信息
            usersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // 将备注添加到 user 对象中
                        String remark = groups.get(phoneNumber);
                        user.addGroupMember(phoneNumber, remark);
                        groupMembers.add(user);
                    }
                    counter[0]--;
                    if (counter[0] == 0) {
                        // 所有数据加载完成，更新适配器
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]--;
                    if (counter[0] == 0) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    // 刷新当前用户的数据
    private void refreshCurrentUserData() {
        usersRef.child(currentUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            // ... 原有代码
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User updatedUser = snapshot.getValue(User.class);
                if (updatedUser != null) {
                    login_activity.currentUser = updatedUser;
                    displayGroupMembers();
                } else {
                    // 如果用户数据为空，清空列表并更新适配器
                    groupMembers.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Contacts.this, getString(R.string.database_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 编辑按钮点击事件
    private void onEditClicked(User user) {
        final EditText input = new EditText(this);
        input.setText(user.getGroups().get(user.username));
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_remark)
                .setView(input)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String newRemark = input.getText().toString().trim();
                    updateRemark(user.username, newRemark);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 更新备注
    private void updateRemark(String phoneNumber, String newRemark) {
        usersRef.child(currentUserPhone).child("groups").child(phoneNumber).setValue(newRemark);
        login_activity.currentUser.updateRemark(phoneNumber, newRemark);
        // 调用刷新方法
        refreshCurrentUserData();
    }

    // 删除按钮点击事件
    private void onDeleteClicked(User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    deleteGroupMember(user.username);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 删除关联用户
    private void deleteGroupMember(String phoneNumber) {
        // 删除当前用户 groups 节点下的关联用户
        usersRef.child(currentUserPhone).child("groups").child(phoneNumber).removeValue();
        login_activity.currentUser.removeGroupMember(phoneNumber);
        // 同时删除对方 groups 节点下的当前用户
        usersRef.child(phoneNumber).child("groups").child(currentUserPhone).removeValue();
        // 调用刷新方法
        refreshCurrentUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新显示关联的用户信息
        if (isLoggedIn() && login_activity.currentUser != null) {
            refreshCurrentUserData();
        }
        Log.d(Tag, "onResume: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Tag, "onStart: ");
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