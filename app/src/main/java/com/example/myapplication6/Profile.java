package com.example.myapplication6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private ImageView mB_home;
    private TextView text_username;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

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
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(Profile.this, Home.class);
                startActivity(homeIntent);
            } else if (item.getItemId() == R.id.Setting) {
                Intent settingIntent = new Intent(Profile.this, Setting.class);
                startActivity(settingIntent);
            } else if (item.getItemId() == R.id.Contacts) {
                Intent contactsIntent = new Intent(Profile.this,  Contacts.class);
                startActivity(contactsIntent);
            }
            return true;
        });

        mB_home = findViewById(R.id.back_button);
        mB_home.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Setting.class);
            startActivity(intent);
        });

        updateButton();

        if (isLoggedIn() && login_activity.currentUser == null) {
            String username = sharedPreferences.getString(KEY_USERNAME, null);
            if (username != null) {
                retrieveCurrentUser(username);
            }
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void updateButton() {

        text_username = findViewById(R.id.username);
        if (isLoggedIn()) {
            if (login_activity.currentUser != null) {
                String username = login_activity.currentUser.username;

                text_username.setText(username);
            } else {
                // 尝试从 SharedPreferences 中获取用户名
                String username = sharedPreferences.getString(KEY_USERNAME, "User");

                text_username.setText(username);
                // 您也可以在此处重新初始化 currentUser
                retrieveCurrentUser(username);
            }
        } else {

            text_username.setText(R.string.not_logged_in);
        }
    }

    private void retrieveCurrentUser(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    login_activity.currentUser = user;
                    // 更新用户名显示
                    text_username.setText(user.username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, getString(R.string.database_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}