package com.example.tripbadu;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EditGearActivity extends AppCompatActivity {

    private RecyclerView rvEditGear;
    private LinearLayout llEmptyState;
    private ImageView ivBack;
    private EditGearAdapter adapter;
    private List<Gear> approvedGear = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gear);

        dbHelper = new DatabaseHelper(this);
        rvEditGear = findViewById(R.id.rvEditGear);
        llEmptyState = findViewById(R.id.llEmptyState);
        ivBack = findViewById(R.id.ivBack);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        rvEditGear.setLayoutManager(new LinearLayoutManager(this));
        loadApprovedGear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApprovedGear();
    }

    private void updateEmptyState() {
        if (approvedGear.isEmpty()) {
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
            if (rvEditGear != null) rvEditGear.setVisibility(View.GONE);
        } else {
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            if (rvEditGear != null) rvEditGear.setVisibility(View.VISIBLE);
        }
    }

    private void loadApprovedGear() {
        approvedGear.clear();
        Cursor cursor = dbHelper.getAllGear();
        if (cursor != null) {
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
                    String owner = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_OWNER));

                    approvedGear.add(new Gear(id, name, price, image, lat, lng, contact, status, owner));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        updateEmptyState();
        adapter = new EditGearAdapter(this, approvedGear, gear -> updateEmptyState());
        rvEditGear.setAdapter(adapter);
    }
}
