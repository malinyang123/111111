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
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private static final String GEOJSON_URL = "https://services2.arcgis.com/dEKgZETqwmDAh1rP/arcgis/rest/services/Disability_permit_parking/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson";

    private MapView mapView;
    private GoogleMap googleMap;
    private OkHttpClient client = new OkHttpClient();
    private byte[] responseIN;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView mBtnFirst;
    FloatingActionButton fab;
    // Firebase Database reference
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

        updateUserLocation();
    }

    // Update user location
    private void updateUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateUserLocationInDatabase(latitude, longitude);
                    }
                }
            });
        }
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

        getGroupMembersInfo(new OnGroupMembersInfoRetrievedListener() {
            @Override
            public void onGroupMembersInfoRetrieved(List<GroupMemberInfo> groupMembers) {
                if (groupMembers != null && !groupMembers.isEmpty()) {
                    for (GroupMemberInfo member : groupMembers) {
                        LinearLayout memberLayout = new LinearLayout(Map.this);
                        memberLayout.setOrientation(LinearLayout.HORIZONTAL);
                        memberLayout.setPadding(10, 10, 10, 10);

                        ImageView memberIcon = new ImageView(Map.this);
                        memberIcon.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        memberIcon.setImageResource(R.drawable.user1); // Default user icon
                        memberLayout.addView(memberIcon);

                        TextView memberName = new TextView(Map.this);
                        memberName.setText(member.remark != null ? member.remark : member.phoneNumber);
                        memberName.setTextSize(25);
                        memberName.setTextColor(getResources().getColor(R.color.black));
                        memberName.setPadding(30, 0, 0, 0);
                        memberLayout.addView(memberName);

                        TextView memberLocation = new TextView(Map.this);
                        if (member.latitude != null && member.longitude != null) {
                            memberLocation.setText("Lat: " + member.latitude + ", Lon: " + member.longitude);
                            LatLng memberLatLng = new LatLng(member.latitude, member.longitude);

                            // Add marker for group member location
                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(memberLatLng)
                                    .title(member.remark)
                                    .snippet("Group member location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            marker.setTag("group_member");

                            // Fetch and display rainfall for group members
                            fetchFacilityRainfall(member.latitude, member.longitude, marker);

                        } else {
                            memberLocation.setText("Not Logged In");
                        }
                        memberLocation.setTextSize(25);
                        memberLocation.setTextColor(getResources().getColor(R.color.black));
                        memberLocation.setPadding(90, 0, 0, 0);
                        memberLayout.addView(memberLocation);

                        groupMemberList.addView(memberLayout);

                        View divider = new View(Map.this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1
                        ));
                        divider.setBackgroundColor(getResources().getColor(R.color.black));
                        groupMemberList.addView(divider);
                    }
                } else {
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
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            showCurrentLocationWeather();
            fetchGeoJsonData();  // Fetch and display GeoJSON data for accessibility facilities
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
                    showRainfallInputDialog(marker);
                } else if (marker.getTag().equals("group_member")) {
                    showRainfallInputDialogForGroup(marker);
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

    private void fetchWeatherData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
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

    private void displayRainfallOnMap(byte[] responseIN, double latitude, double longitude) {
        ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);

        WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
        VariablesWithTime minutely15 = mApiResponse.minutely15();
        VariableWithValues precipitation = new VariablesSearch(minutely15)
                .variable(Variable.precipitation)
                .first();

        LatLng currentLocation = new LatLng(latitude, longitude);

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Current location")
                .snippet("Precipitation: " + precipitation.values(0) + " mm")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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

    private void parseAndAddGeoJsonMarkers(JSONObject geoJsonData) {
        try {
            JSONArray features = geoJsonData.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);

                JSONObject properties = feature.getJSONObject("properties");
                String facilityName = properties.optString("name", "Accessibility");

                LatLng location = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(facilityName)
                        .snippet("Accessibility Facility")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // 无障碍设施不需要涂色
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error adding markers from GeoJSON", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void displayFacilityRainfall(byte[] responseIN, Marker marker) {
        ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);

        WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
        VariablesWithTime minutely15 = mApiResponse.minutely15();
        VariableWithValues precipitation = new VariablesSearch(minutely15)
                .variable(Variable.precipitation)
                .first();

        float precipitationValue = precipitation.values(0);
        marker.setSnippet("Precipitation: " + precipitationValue + " mm");
        marker.showInfoWindow();

        // 群组用户需要涂色
        LatLng memberLatLng = marker.getPosition();
        CircleOptions circleOptions = new CircleOptions()
                .center(memberLatLng)
                .radius(100)
                .strokeColor(Color.BLACK)
                .fillColor(getColorForRainfall(precipitationValue))
                .strokeWidth(5);
        googleMap.addCircle(circleOptions);

        // 检查洪水风险
        checkFloodRisk(precipitationValue, memberLatLng);
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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
            warningMessage = "Group member's location has a high risk of flooding with rainfall exceeding 50mm. Please take precautions.";
            new AlertDialog.Builder(this)
                    .setTitle("Flood risk")
                    .setMessage(warningMessage)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
            warningMessage = "Group member's location has a moderate risk of flooding with rainfall between 25mm and 50mm.";
            new AlertDialog.Builder(this)
                    .setTitle("Flood risk")
                    .setMessage(warningMessage)
                    .setPositiveButton("Confirm", null)
                    .show();
        }

        if (warningMessage != null) {
            googleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title("Group member flood warning")
                            .snippet(warningMessage))
                    .showInfoWindow();
        }
    }

    private int getColorForRainfall(float precipitation) {
        if (precipitation <= 10) {
            return Color.argb(100, 173, 216, 230);  // Light blue
        } else if (precipitation <= 25) {
            return Color.argb(150, 255, 102, 102);  // Light red
        } else if (precipitation <= 50) {
            return Color.argb(150, 255, 0, 0);  // Dark red
        } else {
            return Color.argb(150, 139, 0, 0);  // Very dark red
        }
    }

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

    private void showRainfallInputDialogForGroup(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set rainfall for group member");

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
                newMarker.setTag("group_member");
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

    private void updateUserLocationInDatabase(double latitude, double longitude) {
        if (login_activity.currentUser != null) {
            String username = login_activity.currentUser.username;
            usersRef.child(username).child("latitude").setValue(latitude);
            usersRef.child(username).child("longitude").setValue(longitude);
            login_activity.currentUser.setLocation(latitude, longitude);
        } else {
            Toast.makeText(Map.this, "Please log in to update location", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Map.this, login_activity.class);
            startActivity(intent);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(false);
            }
        }
    }

    public void getGroupMembersInfo(OnGroupMembersInfoRetrievedListener listener) {
        if (login_activity.currentUser == null) {
            listener.onGroupMembersInfoRetrieved(null);
            return;
        }
        String currentUserPhone = login_activity.currentUser.username;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(currentUserPhone).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<GroupMemberInfo> groupMembers = new ArrayList<>();
                    final int[] counter = {(int) snapshot.getChildrenCount()};

                    if (counter[0] == 0) {
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
                                    Double latitude = contactSnapshot.child("latitude").getValue(Double.class);
                                    Double longitude = contactSnapshot.child("longitude").getValue(Double.class);

                                    if (latitude == null || longitude == null) {
                                        GroupMemberInfo memberInfo = new GroupMemberInfo(
                                                contactPhone,
                                                null,
                                                null,
                                                remark
                                        );
                                        groupMembers.add(memberInfo);
                                    } else {
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
                    listener.onGroupMembersInfoRetrieved(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onGroupMembersInfoRetrieved(null);
            }
        });
    }

    public interface OnGroupMembersInfoRetrievedListener {
        void onGroupMembersInfoRetrieved(List<GroupMemberInfo> groupMembers);
    }

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
