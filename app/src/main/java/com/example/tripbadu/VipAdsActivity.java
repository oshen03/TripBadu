package com.example.tripbadu;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class VipAdsActivity extends AppCompatActivity {

    private RecyclerView rvMyAds;
    private VipAdsAdapter adapter;
    private List<Gear> myAds = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_ads);

        dbHelper = new DatabaseHelper(this);
        rvMyAds = findViewById(R.id.rvMyAds);
        rvMyAds.setLayoutManager(new LinearLayoutManager(this));

        loadMyAds();
    }

    private void loadMyAds() {
        myAds.clear();
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString("email", "");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_GEAR + " WHERE " + DatabaseHelper.COLUMN_GEAR_OWNER + " = ?", new String[]{userEmail});
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_IMAGE));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LNG));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_CONTACT));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_STATUS));
                
                myAds.add(new Gear(id, name, price, image, lat, lng, contact, status, userEmail));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new VipAdsAdapter(this, myAds, this::loadMyAds);
        rvMyAds.setAdapter(adapter);
    }
}
