package com.example.tripbadu;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdApprovalActivity extends AppCompatActivity {

    private RecyclerView rvPendingAds;
    private AdApprovalAdapter adapter;
    private List<Gear> pendingAds = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_approval);

        dbHelper = new DatabaseHelper(this);
        rvPendingAds = findViewById(R.id.rvPendingAds);
        rvPendingAds.setLayoutManager(new LinearLayoutManager(this));

        loadPendingAds();
    }

    private void loadPendingAds() {
        pendingAds.clear();
        Cursor cursor = dbHelper.getPendingGear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_IMAGE));
                pendingAds.add(new Gear(id, name, price, image));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new AdApprovalAdapter(this, pendingAds);
        rvPendingAds.setAdapter(adapter);
    }
}
