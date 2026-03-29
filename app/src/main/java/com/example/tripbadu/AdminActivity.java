package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class AdminActivity extends AppCompatActivity {

    private EditText etGearName, etGearPrice;
    private ImageView ivGearPreview, ivAdminLogout;
    private Button btnSelectImage, btnAddGear, btnAdApproval, btnManageMembers, btnViewMain;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        etGearName = findViewById(R.id.etGearName);
        etGearPrice = findViewById(R.id.etGearPrice);
        ivGearPreview = findViewById(R.id.ivGearPreview);
        ivAdminLogout = findViewById(R.id.ivAdminLogout);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddGear = findViewById(R.id.btnAddGear);
        btnAdApproval = findViewById(R.id.btnAdApproval);
        btnManageMembers = findViewById(R.id.btnManageMembers);
        btnViewMain = findViewById(R.id.btnViewMain);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivGearPreview.setImageURI(selectedImageUri);
                        ivGearPreview.setVisibility(View.VISIBLE);
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnAddGear.setOnClickListener(v -> {
            String name = etGearName.getText().toString().trim();
            String priceStr = etGearPrice.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            } else {
                double price = Double.parseDouble(priceStr);
                dbHelper.addGear(name, price, selectedImageUri.toString(), "approved");
                Toast.makeText(this, "Gear Added Successfully", Toast.LENGTH_SHORT).show();
                
                etGearName.setText("");
                etGearPrice.setText("");
                ivGearPreview.setVisibility(View.GONE);
                selectedImageUri = null;
            }
        });

        ivAdminLogout.setOnClickListener(v -> logout());

        btnAdApproval.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Ad Approvals...", Toast.LENGTH_SHORT).show();
        });

        btnManageMembers.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Member Management...", Toast.LENGTH_SHORT).show();
        });

        btnViewMain.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, HomeActivity.class));
            finish();
        });
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
