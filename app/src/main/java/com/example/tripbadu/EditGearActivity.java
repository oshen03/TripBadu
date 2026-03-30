package com.example.tripbadu;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EditGearActivity extends AppCompatActivity {

    private RecyclerView rvEditGear;
    private EditGearAdapter adapter;
    private List<Gear> approvedGear = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gear);

        dbHelper = new DatabaseHelper(this);
        rvEditGear = findViewById(R.id.rvEditGear);
        rvEditGear.setLayoutManager(new LinearLayoutManager(this));

        loadApprovedGear();
    }

    private void loadApprovedGear() {
        approvedGear.clear();
        Cursor cursor = dbHelper.getAllGear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_IMAGE));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LNG));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_CONTACT));
                
                approvedGear.add(new Gear(id, name, price, image, lat, lng, contact));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new EditGearAdapter(this, approvedGear);
        rvEditGear.setAdapter(adapter);
    }
}
