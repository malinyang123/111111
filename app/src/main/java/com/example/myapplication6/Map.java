package com.example.myapplication6;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.Variable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
// 导入 Firebase 数据库相关类
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private static final String GEOJSON_URL = "https://services2.arcgis.com/dEKgZETqwmDAh1rP/arcgis/rest/services/Disability_permit_parking/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson";

    private MapView mapView;
    private GoogleMap googleMap;
    private OkHttpClient client = new OkHttpClient();
    private byte[] responseIN;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView mBtnFirst;
    FloatingActionButton fab;
    // 新增：Firebase 数据库引用
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fab = findViewById(R.id.fab);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            fetchWeatherData();
        }

        mBtnFirst = findViewById(R.id.Return1);
        mBtnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map.this, Home.class);
                startActivity(intent);
            }
        });
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout groupMemberList = dialog.findViewById(R.id.group_member_list);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // 调用获取群组成员信息的逻辑
        getGroupMembersInfo(new OnGroupMembersInfoRetrievedListener() {
            @Override
            public void onGroupMembersInfoRetrieved(List<GroupMemberInfo> groupMembers) {
                if (groupMembers != null && !groupMembers.isEmpty()) {
                    for (GroupMemberInfo member : groupMembers) {
                        // 创建动态视图来显示群组成员信息
                        LinearLayout memberLayout = new LinearLayout(Map.this);
                        memberLayout.setOrientation(LinearLayout.HORIZONTAL);
                        memberLayout.setPadding(10, 10, 10, 10);

                        // 添加成员头像
                        ImageView memberIcon = new ImageView(Map.this);
                        memberIcon.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        memberIcon.setImageResource(R.drawable.user1); // 使用默认头像
                        memberLayout.addView(memberIcon);

                        // 显示成员名字
                        TextView memberName = new TextView(Map.this);
                        memberName.setText(member.remark != null ? member.remark : member.phoneNumber);
                        memberName.setTextSize(25);
                        memberName.setTextColor(getResources().getColor(R.color.black));
                        memberName.setPadding(30, 0, 0, 0);
                        memberLayout.addView(memberName);

                        // 如果存在经纬度信息，则显示地址，否则显示未登录提示
                        TextView memberLocation = new TextView(Map.this);
                        if (member.latitude != null && member.longitude != null) {
                            memberLocation.setText("Location Set");  // 你可以根据具体情况显示地点名
                        } else {
                            memberLocation.setText("Not Logged In");
                        }
                        memberLocation.setTextSize(25);
                        memberLocation.setTextColor(getResources().getColor(R.color.black));
                        memberLocation.setPadding(90, 0, 0, 0);
                        memberLayout.addView(memberLocation);

                        // 将生成的成员布局添加到列表中
                        groupMemberList.addView(memberLayout);

                        // 添加分割线
                        View divider = new View(Map.this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1
                        ));
                        divider.setBackgroundColor(getResources().getColor(R.color.black));
                        groupMemberList.addView(divider);
                    }
                } else {
                    // 如果群组成员信息为空，则只显示提示
                    TextView noMemberInfo = new TextView(Map.this);
                    noMemberInfo.setText("No group members available");
                    noMemberInfo.setTextSize(20);
                    noMemberInfo.setPadding(10, 10, 10, 10);
                    groupMemberList.addView(noMemberInfo);
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            showCurrentLocationWeather();
            fetchGeoJsonData();  // 获取并显示 GeoJSON 数据
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag().equals("current_location")) {
                    showRainfallInputDialog(marker);  // 只有当前位置可以手动设置降水量
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    showCurrentLocationWeather();
                }
            }
        }
    }

    // 获取当前天气数据
    private void fetchWeatherData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // 使用 OpenMeteo API 获取降水量数据
                                String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                                        + "&longitude=" + longitude
                                        + "&timezone=Australia/Brisbane&minutely_15=precipitation&format=flatbuffers";

                                Request request = new Request.Builder()
                                        .url(url)
                                        .method("GET", null)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            responseIN = response.body().bytes();
                                            runOnUiThread(() -> displayRainfallOnMap(responseIN, latitude, longitude));
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(Map.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // 显示当前地理位置的降雨量数据
    private void showCurrentLocationWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                updateUserLocationInDatabase(latitude, longitude);

                                fetchCurrentLocationWeather(latitude, longitude);
                            } else {
                                Toast.makeText(Map.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // 在地图上显示降雨量
    private void displayRainfallOnMap(byte[] responseIN, double latitude, double longitude) {
        // 解析从 OpenMeteo 获取的二进制数据
        ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);

        // 通过 WeatherApiResponse 获取天气数据
        WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
        VariablesWithTime minutely15 = mApiResponse.minutely15();
        VariableWithValues precipitation = new VariablesSearch(minutely15)
                .variable(Variable.precipitation)
                .first();

        LatLng currentLocation = new LatLng(latitude, longitude);

        // 在地图上添加一个标记，当前位置为红色
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Current location")
                .snippet("Precipitation: " + precipitation.values(0) + " mm")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // 红色标记
        marker.setTag("current_location");
        marker.showInfoWindow();

        // 绘制表示降雨量的圆圈，颜色根据降雨量不同而变化
        CircleOptions circleOptions = new CircleOptions()
                .center(currentLocation)
                .radius(100)  // 半径
                .strokeColor(Color.BLACK)  // 圆圈边缘颜色
                .fillColor(getColorForRainfall(precipitation.values(0)))  // 圆圈填充颜色，基于降雨量变化
                .strokeWidth(5);  // 边缘宽度
        googleMap.addCircle(circleOptions);

        // 检查是否存在洪水风险
        checkFloodRisk(precipitation.values(0), currentLocation);

        buffer.clear();
    }

    // Fetch GeoJSON data from ArcGIS
    private void fetchGeoJsonData() {
        Request request = new Request.Builder()
                .url(GEOJSON_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(Map.this, "Failed to load GeoJSON data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String geoJsonString = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject geoJsonData = new JSONObject(geoJsonString);
                            parseAndAddGeoJsonMarkers(geoJsonData);
                        } catch (JSONException e) {
                            Toast.makeText(Map.this, "Failed to parse GeoJSON", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Parse the GeoJSON and add markers manually
    private void parseAndAddGeoJsonMarkers(JSONObject geoJsonData) {
        try {
            JSONArray features = geoJsonData.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);

                // Extract properties like "name" if present
                JSONObject properties = feature.getJSONObject("properties");
                String facilityName = properties.optString("name", "Accessibility");

                // Add a blue marker for accessibility facilities
                LatLng location = new LatLng(latitude, longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(facilityName)
                        .snippet("Accessibility Facility")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // 蓝色标记
                marker.setTag("facility_location");

                // Fetch rainfall data for the facility
                fetchFacilityRainfall(latitude, longitude, marker);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error adding markers from GeoJSON", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch rainfall data for facility location and display it on the marker
    private void fetchFacilityRainfall(double latitude, double longitude, Marker marker) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                + "&longitude=" + longitude
                + "&timezone=Australia/Brisbane&minutely_15=precipitation&format=flatbuffers";

        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    byte[] responseIN = response.body().bytes();
                    runOnUiThread(() -> displayFacilityRainfall(responseIN, marker));
                }
            }
        });
    }

    // Display rainfall data for facility location on the marker
    private void displayFacilityRainfall(byte[] responseIN, Marker marker) {
        ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);

        WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
        VariablesWithTime minutely15 = mApiResponse.minutely15();
        VariableWithValues precipitation = new VariablesSearch(minutely15)
                .variable(Variable.precipitation)
                .first();

        // Update marker snippet with precipitation data
        marker.setSnippet("Precipitation: " + precipitation.values(0) + " mm");
        marker.showInfoWindow();

        buffer.clear();
    }

    private void fetchCurrentLocationWeather(double latitude, double longitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                + "&longitude=" + longitude
                + "&timezone=Australia/Brisbane&minutely_15=precipitation&format=flatbuffers";

        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    responseIN = response.body().bytes();
                    runOnUiThread(() -> displayCurrentLocationWeather(responseIN, latitude, longitude));
                }
            }
        });
    }

    private void displayCurrentLocationWeather(byte[] responseIN, double latitude, double longitude) {
        ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);

        WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
        VariablesWithTime minutely15 = mApiResponse.minutely15();
        VariableWithValues precipitation = new VariablesSearch(minutely15)
                .variable(Variable.precipitation)
                .first();

        LatLng currentLocation = new LatLng(latitude, longitude);
        String precipitationText = "Precipitation: " + precipitation.values(0) + " mm";

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Current location")
                .snippet(precipitationText)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // 红色标记
        marker.setTag("current_location");
        marker.showInfoWindow();

        CircleOptions circleOptions = new CircleOptions()
                .center(currentLocation)
                .radius(100)
                .strokeColor(Color.BLACK)
                .fillColor(getColorForRainfall(precipitation.values(0)))
                .strokeWidth(5);
        googleMap.addCircle(circleOptions);

        checkFloodRisk(precipitation.values(0), currentLocation);

        buffer.clear();
    }

    private void checkFloodRisk(float precipitation, LatLng location) {
        String warningMessage = null;

        if (precipitation > 50) {
            warningMessage = "The current rainfall is greater than 50mm, and there is a high probability of flooding! Please pay attention to safety.";
            new AlertDialog.Builder(this)
                    .setTitle("Flood risk")
                    .setMessage(warningMessage)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转到 AlertNotification 页面，并传递数据
                            Intent intent = new Intent(Map.this, AlertNotification.class);
                            intent.putExtra("precipitation", precipitation);
                            intent.putExtra("latitude", location.latitude);
                            intent.putExtra("longitude", location.longitude);
                            intent.putExtra("timestamp", System.currentTimeMillis());
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (precipitation >= 25 && precipitation <= 50) {
            warningMessage = "The current precipitation is between 25mm and 50mm. There is a high probability of flooding, so please take precautions.";
            new AlertDialog.Builder(this)
                    .setTitle("Flood risk")
                    .setMessage(warningMessage)
                    .setPositiveButton("Confirm", null)
                    .show();
        }

        if (warningMessage != null) {
            googleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title("Flood risk warning")
                            .snippet(warningMessage))
                    .showInfoWindow();
        }
    }

    private int getColorForRainfall(float precipitation) {
        if (precipitation <= 10) {
            return Color.argb(100, 173, 216, 230);
        } else if (precipitation <= 25) {
            return Color.argb(150, 255, 102, 102);
        } else if (precipitation <= 50) {
            return Color.argb(150, 255, 0, 0);
        } else {
            return Color.argb(150, 139, 0, 0);
        }
    }

    // 只有当前位置才可以手动设置降水量
    private void showRainfallInputDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Setting the rainfall");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float manualRainfall = Float.parseFloat(input.getText().toString());
                marker.setSnippet("Precipitation: " + manualRainfall + " mm");
                marker.showInfoWindow();
                marker.remove();
                CircleOptions circleOptions = new CircleOptions()
                        .center(marker.getPosition())
                        .radius(100)
                        .strokeColor(Color.BLACK)
                        .fillColor(getColorForRainfall(manualRainfall))
                        .strokeWidth(5);
                Marker newMarker = googleMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .snippet(marker.getSnippet())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                newMarker.setTag("current_location");
                googleMap.addCircle(circleOptions);
                checkFloodRisk(manualRainfall, marker.getPosition());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // 新增方法：更新数据库中的用户位置
    private void updateUserLocationInDatabase(double latitude, double longitude) {
        // 检查用户是否已登录
        if (login_activity.currentUser != null) {
            String username = login_activity.currentUser.username;
            // 更新数据库中的经纬度
            usersRef.child(username).child("latitude").setValue(latitude);
            usersRef.child(username).child("longitude").setValue(longitude);
            // 可选：更新本地的 currentUser 对象
            login_activity.currentUser.setLocation(latitude, longitude);
        } else {
            // 用户未登录，提示用户登录，并阻止定位功能
            Toast.makeText(Map.this, "Please log in to update location", Toast.LENGTH_SHORT).show();
            // 引导用户到登录页面
            Intent intent = new Intent(Map.this, login_activity.class);
            startActivity(intent);
            // 禁用获取用户位置
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(false); // 禁用地图的用户位置功能
            }
        }
    }

    // New method: Get group members' information
    //如何使用下列代码获取用户信息：
//    getGroupMembersInfo(new OnGroupMembersInfoRetrievedListener() {
//        @Override
//        public void onGroupMembersInfoRetrieved(List<GroupMemberInfo> groupMembers) {
//            if (groupMembers == null) {
//                // 用户未登录
//                Toast.makeText(Map.this, "请先登录", Toast.LENGTH_SHORT).show();
//            } else if (groupMembers.isEmpty()) {
//                // 未找到群组成员
//                Toast.makeText(Map.this, "未找到群组成员", Toast.LENGTH_SHORT).show();
//            } else {
//                // 处理群组成员信息
//                for (GroupMemberInfo member : groupMembers) {
//                    // 例如，在地图上添加标记
//                    if (member.latitude != null && member.longitude != null) {
//                        LatLng memberLocation = new LatLng(member.latitude, member.longitude);
//                        googleMap.addMarker(new MarkerOptions()
//                                .position(memberLocation)
//                                .title("用户: " + member.phoneNumber)
//                                .snippet("备注: " + member.remark));
//                    }
//                }
//            }
//        }
//    });
    public void getGroupMembersInfo(OnGroupMembersInfoRetrievedListener listener) {
        if (login_activity.currentUser == null) {
            // 用户未登录，通过回调返回 null
            listener.onGroupMembersInfoRetrieved(null);
            return;
        }
        String currentUserPhone = login_activity.currentUser.username;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 获取当前用户的 groups
        usersRef.child(currentUserPhone).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<GroupMemberInfo> groupMembers = new ArrayList<>();
                    final int[] counter = {(int) snapshot.getChildrenCount()};

                    if (counter[0] == 0) {
                        // 未找到群组成员
                        listener.onGroupMembersInfoRetrieved(groupMembers);
                        return;
                    }

                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String contactPhone = childSnapshot.getKey();
                        String remark = childSnapshot.getValue(String.class);

                        usersRef.child(contactPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot contactSnapshot) {
                                if (contactSnapshot.exists()) {
                                    // 获取群组成员的经纬度信息
                                    Double latitude = contactSnapshot.child("latitude").getValue(Double.class);
                                    Double longitude = contactSnapshot.child("longitude").getValue(Double.class);

                                    // 判断是否存在经纬度信息
                                    if (latitude == null || longitude == null) {
                                        // 经纬度为空，说明该群组成员未登录或未设置位置
                                        GroupMemberInfo memberInfo = new GroupMemberInfo(
                                                contactPhone,
                                                null,
                                                null,
                                                remark
                                        );
                                        groupMembers.add(memberInfo);
                                    } else {
                                        // 经纬度存在，说明该群组成员已登录并设置了位置
                                        GroupMemberInfo memberInfo = new GroupMemberInfo(
                                                contactPhone,
                                                latitude,
                                                longitude,
                                                remark
                                        );
                                        groupMembers.add(memberInfo);
                                    }
                                }
                                counter[0]--;
                                if (counter[0] == 0) {
                                    listener.onGroupMembersInfoRetrieved(groupMembers);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                counter[0]--;
                                if (counter[0] == 0) {
                                    listener.onGroupMembersInfoRetrieved(groupMembers);
                                }
                            }
                        });
                    }
                } else {
                    // 未找到 groups 节点
                    listener.onGroupMembersInfoRetrieved(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 发生错误
                listener.onGroupMembersInfoRetrieved(null);
            }
        });
    }

    // Callback interface
    public interface OnGroupMembersInfoRetrievedListener {
        void onGroupMembersInfoRetrieved(List<GroupMemberInfo> groupMembers);
    }
    // Data class for group member information
    public static class GroupMemberInfo {
        public String phoneNumber;
        public Double latitude;
        public Double longitude;
        public String remark;
        public GroupMemberInfo(String phoneNumber, Double latitude, Double longitude, String remark) {
            this.phoneNumber = phoneNumber;
            this.latitude = latitude;
            this.longitude = longitude;
            this.remark = remark;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
