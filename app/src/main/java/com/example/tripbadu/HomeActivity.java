package com.example.tripbadu;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private Button btnViewCart, btnViewMap;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        rvGear = findViewById(R.id.rvGear);
        btnViewCart = findViewById(R.id.btnViewCart);
        btnViewMap = findViewById(R.id.btnViewMap);

        rvGear.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GearAdapter(this, gearList);
        rvGear.setAdapter(adapter);

        // Start SOS Service
        startService(new Intent(this, SosService.class));

        loadLocalGear(); // Load from local DB
        fetchRemoteGear(); // Also try to fetch from remote

        btnViewCart.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));
        btnViewMap.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MapActivity.class)));
    }

    private void loadLocalGear() {
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
        adapter.notifyDataSetChanged();
    }

    private void fetchRemoteGear() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getGear().enqueue(new Callback<List<Gear>>() {
            @Override
            public void onResponse(Call<List<Gear>> call, Response<List<Gear>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // For now we merge them, or you could clear local first
                    gearList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Gear>> call, Throwable t) {
                // If remote fails, we already have local data
            }
        });
    }
}
