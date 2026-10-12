package com.example.tripbadu;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdApprovalActivity extends AppCompatActivity {

    private RecyclerView rvPendingAds;
    private LinearLayout llEmptyState;
    private ImageView ivBack;
    private AdApprovalAdapter adapter;
    private List<Gear> pendingAds = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_approval);

        dbHelper = new DatabaseHelper(this);
        rvPendingAds = findViewById(R.id.rvPendingAds);
        llEmptyState = findViewById(R.id.llEmptyState);
        ivBack = findViewById(R.id.ivBack);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        rvPendingAds.setLayoutManager(new LinearLayoutManager(this));
        loadPendingAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingAds();
    }

    private void loadPendingAds() {
        pendingAds.clear();
        Cursor cursor = dbHelper.getPendingGear();
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

                    pendingAds.add(new Gear(id, name, price, image, lat, lng, contact, status, owner));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        updateEmptyState();
        adapter = new AdApprovalAdapter(this, pendingAds, this::updateEmptyState);
        rvPendingAds.setAdapter(adapter);

        // Also fetch pending ads from Firebase Cloud
        FirebaseHelper.getInstance().getPendingGearQuery().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean changed = false;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Gear gear = child.getValue(Gear.class);
                        if (gear != null) {
                            dbHelper.insertOrUpdateGear(gear);
                            boolean exists = false;
                            for (Gear p : pendingAds) {
                                if (p.getId() == gear.getId()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                pendingAds.add(gear);
                                changed = true;
                            }
                        }
                    }
                    if (changed && adapter != null) {
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateEmptyState() {
        if (pendingAds.isEmpty()) {
            rvPendingAds.setVisibility(View.GONE);
            if (llEmptyState != null) {
                llEmptyState.setVisibility(View.VISIBLE);
            }
        } else {
            rvPendingAds.setVisibility(View.VISIBLE);
            if (llEmptyState != null) {
                llEmptyState.setVisibility(View.GONE);
            }
        }
    }
}
