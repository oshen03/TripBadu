package com.example.tripbadu;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvGear;
    private GearAdapter adapter;
    private List<Gear> gearList = new ArrayList<>();
    private Button btnViewCart, btnViewMap, btnAdminPanel, btnMyGarage;
    private ImageView ivLogout;
    private EditText etSearch;
    private DatabaseHelper dbHelper;

    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;

    // Dynamic BroadcastReceiver instance (registered in onResume, unregistered in onPause)
    private NetworkChangeReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        rvGear = findViewById(R.id.rvGear);
        btnViewCart = findViewById(R.id.btnViewCart);
        btnViewMap = findViewById(R.id.btnViewMap);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        btnMyGarage = findViewById(R.id.btnMyGarage);
        ivLogout = findViewById(R.id.ivLogout);
        etSearch = findViewById(R.id.etSearch);

        checkUserRole();

        rvGear.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GearAdapter(this, gearList);
        rvGear.setAdapter(adapter);

        // Runtime permissions: SMS (for SOS) + Notifications (Android 13+)
        List<String> permsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!permsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permsNeeded.toArray(new String[0]), 101);
        }

        ContextCompat.startForegroundService(this, new Intent(this, SosService.class));

        loadLocalGear();
        fetchRemoteGear();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnViewCart.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        btnViewMap.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MapActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        ivLogout.setOnClickListener(v -> logout());

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(() ->
            Snackbar.make(findViewById(android.R.id.content), "Device shaken! SOS monitoring active. 🚀", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.primary))
                    .setTextColor(getResources().getColor(R.color.on_primary))
                    .show()
        );

        // Prepare dynamic BroadcastReceiver
        networkReceiver = new NetworkChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh role-based button visibility depending on session
        checkUserRole();

        // Register sensor listener
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Register BroadcastReceiver dynamically (preferred for foreground-only monitoring)
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private void checkUserRole() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String role = sharedPref.getString("role", "");
        if ("admin".equalsIgnoreCase(role)) {
            btnAdminPanel.setVisibility(View.VISIBLE);
            btnAdminPanel.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, AdminActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }

        if ("vip".equalsIgnoreCase(role)) {
            btnMyGarage.setVisibility(View.VISIBLE);
            btnMyGarage.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, VipDashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        } else {
            btnMyGarage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException ignored) {}
        super.onPause();
    }

    private void loadLocalGear() {
        gearList.clear();
        Cursor cursor = dbHelper.getAllGear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_IMAGE));
                gearList.add(new Gear(id, name, price, image));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.updateList(gearList);
    }

    private void fetchRemoteGear() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.8.113:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getGear().enqueue(new Callback<List<Gear>>() {
            @Override
            public void onResponse(Call<List<Gear>> call, Response<List<Gear>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gearList.addAll(response.body());
                    adapter.updateList(gearList);
                }
            }
            @Override
            public void onFailure(Call<List<Gear>> call, Throwable t) {
                // Remote fetch failed — local data shown; network receiver will notify user
            }
        });
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
