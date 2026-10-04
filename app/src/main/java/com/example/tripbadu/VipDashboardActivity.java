package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VipDashboardActivity extends AppCompatActivity {

    private ImageView ivAdPreview, ivVipLogout, ivNotifications;
    private EditText etAdName, etAdDesc, etAdPrice, etAdLat, etAdLng, etAdContact;
    private Button btnSelectImage, btnTakePhoto, btnSubmitAd, btnGoHome, btnPickLocation, btnManageMyAds;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_dashboard);

        dbHelper = new DatabaseHelper(this);

        ivAdPreview = findViewById(R.id.ivAdPreview);
        ivVipLogout = findViewById(R.id.ivVipLogout);
        ivNotifications = findViewById(R.id.ivNotifications);
        etAdName = findViewById(R.id.etAdName);
        etAdDesc = findViewById(R.id.etAdDesc);
        etAdPrice = findViewById(R.id.etAdPrice);
        etAdLat = findViewById(R.id.etAdLat);
        etAdLng = findViewById(R.id.etAdLng);
        etAdContact = findViewById(R.id.etAdContact);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmitAd = findViewById(R.id.btnSubmitAd);
        btnGoHome = findViewById(R.id.btnGoHome);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnManageMyAds = findViewById(R.id.btnManageMyAds);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        ivAdPreview.setImageURI(selectedImageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        try {
                            // Save bitmap to cache dir and use its URI
                            File cachePath = new File(getCacheDir(), "images");
                            cachePath.mkdirs();
                            File imageFile = new File(cachePath, "capture_" + System.currentTimeMillis() + ".png");
                            FileOutputStream stream = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            stream.close();
                            selectedImageUri = Uri.fromFile(imageFile);
                            ivAdPreview.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);
                        etAdLat.setText(String.valueOf(lat));
                        etAdLng.setText(String.valueOf(lng));
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        btnTakePhoto.setOnClickListener(v -> {
            cameraLauncher.launch(null);
        });

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(VipDashboardActivity.this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });

        btnSubmitAd.setOnClickListener(v -> uploadAd());

        ivNotifications.setOnClickListener(v -> {
            startActivity(new Intent(VipDashboardActivity.this, NotificationsActivity.class));
        });

        btnManageMyAds.setOnClickListener(v -> {
            startActivity(new Intent(VipDashboardActivity.this, VipAdsActivity.class));
        });

        ivVipLogout.setOnClickListener(v -> logout());

        btnGoHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void uploadAd() {
        String name = etAdName.getText().toString().trim();
        String priceStr = etAdPrice.getText().toString().trim();
        String latStr = etAdLat.getText().toString().trim();
        String lngStr = etAdLng.getText().toString().trim();
        String contact = etAdContact.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || contact.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all required fields, including contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            String userEmail = sharedPref.getString("email", "");

            dbHelper.addGear(name, price, selectedImageUri.toString(), "pending", lat, lng, contact, userEmail);
            Toast.makeText(this, "Ad Submitted for Approval", Toast.LENGTH_SHORT).show();
            
            etAdName.setText("");
            etAdPrice.setText("");
            etAdLat.setText("");
            etAdLng.setText("");
            etAdContact.setText("");
            etAdDesc.setText("");
            ivAdPreview.setImageDrawable(null);
            selectedImageUri = null;
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for price and coordinates", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
