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
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private EditText etGearName, etGearPrice, etGearLat, etGearLng, etGearContact;
    private ImageView ivGearPreview, ivAdminLogout;
    private TextView tvFormTitle;
    private Button btnSelectImage, btnAddGear, btnAdApproval, btnManageMembers, btnViewMain, btnPickLocation, btnEditExisting, btnCancelEdit, btnDeleteAd;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private int editingGearId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etGearName = findViewById(R.id.etGearName);
        etGearPrice = findViewById(R.id.etGearPrice);
        etGearLat = findViewById(R.id.etGearLat);
        etGearLng = findViewById(R.id.etGearLng);
        etGearContact = findViewById(R.id.etGearContact);
        ivGearPreview = findViewById(R.id.ivGearPreview);
        ivAdminLogout = findViewById(R.id.ivAdminLogout);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddGear = findViewById(R.id.btnAddGear);
        btnAdApproval = findViewById(R.id.btnAdApproval);
        btnManageMembers = findViewById(R.id.btnManageMembers);
        btnViewMain = findViewById(R.id.btnViewMain);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnEditExisting = findViewById(R.id.btnEditExisting);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnDeleteAd = findViewById(R.id.btnDeleteAd);

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
                        ivGearPreview.setImageURI(selectedImageUri);
                        ivGearPreview.setVisibility(View.VISIBLE);
                    }
                }
        );

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);
                        etGearLat.setText(String.valueOf(lat));
                        etGearLng.setText(String.valueOf(lng));
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

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });

        btnAddGear.setOnClickListener(v -> {
            String name = etGearName.getText().toString().trim();
            String priceStr = etGearPrice.getText().toString().trim();
            String latStr = etGearLat.getText().toString().trim();
            String lngStr = etGearLng.getText().toString().trim();
            String contact = etGearContact.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || contact.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double price = Double.parseDouble(priceStr);
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);

                    if (editingGearId == -1) {
                        long insertedId = dbHelper.addGear(name, price, selectedImageUri.toString(), "approved", lat, lng, contact, "admin");
                        Gear cloudGear = new Gear((int) insertedId, name, price, selectedImageUri.toString(), lat, lng, contact, "approved", "admin");
                        FirebaseHelper.getInstance().saveGear(cloudGear, null);
                        Toast.makeText(this, "Gear Added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.updateGear(editingGearId, name, price, selectedImageUri.toString(), lat, lng, contact);
                        Gear cloudGear = new Gear(editingGearId, name, price, selectedImageUri.toString(), lat, lng, contact, "approved", "admin");
                        FirebaseHelper.getInstance().saveGear(cloudGear, null);
                        Toast.makeText(this, "Gear Updated Successfully", Toast.LENGTH_SHORT).show();
                        clearEditingState();
                    }

                    resetForm();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers for price and coordinates", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelEdit.setOnClickListener(v -> clearEditingState());

        btnDeleteAd.setOnClickListener(v -> {
            if (editingGearId == -1) return;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Published Ad")
                    .setMessage("Are you sure you want to permanently delete this ad from the marketplace?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int idToDelete = editingGearId;
                        dbHelper.deleteGear(idToDelete);
                        FirebaseHelper.getInstance().deleteGear(idToDelete);
                        Toast.makeText(this, "Ad Deleted Successfully", Toast.LENGTH_SHORT).show();
                        clearEditingState();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        ivAdminLogout.setOnClickListener(v -> logout());

        btnAdApproval.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AdApprovalActivity.class));
        });

        btnManageMembers.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageMembersActivity.class));
        });

        btnEditExisting.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, EditGearActivity.class);
            startActivity(intent);
        });

        btnViewMain.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, HomeActivity.class));
            finish();
        });

        checkIntentForEdit();
    }

    private void checkIntentForEdit() {
        if (getIntent().hasExtra("edit_id")) {
            editingGearId = getIntent().getIntExtra("edit_id", -1);
            etGearName.setText(getIntent().getStringExtra("edit_name"));
            etGearPrice.setText(String.valueOf(getIntent().getDoubleExtra("edit_price", 0)));
            etGearLat.setText(String.valueOf(getIntent().getDoubleExtra("edit_lat", 0)));
            etGearLng.setText(String.valueOf(getIntent().getDoubleExtra("edit_lng", 0)));
            etGearContact.setText(getIntent().getStringExtra("edit_contact"));
            
            String imgPath = getIntent().getStringExtra("edit_image");
            if (imgPath != null && !imgPath.isEmpty()) {
                selectedImageUri = Uri.parse(imgPath);
                ivGearPreview.setImageURI(selectedImageUri);
                ivGearPreview.setVisibility(View.VISIBLE);
            }

            tvFormTitle.setText("Edit Gear Item");
            btnAddGear.setText("Update Gear Item");
            btnCancelEdit.setVisibility(View.VISIBLE);
            btnDeleteAd.setVisibility(View.VISIBLE);
        }
    }

    private void clearEditingState() {
        editingGearId = -1;
        tvFormTitle.setText("Add New Gear");
        btnAddGear.setText("Add Gear Item");
        btnCancelEdit.setVisibility(View.GONE);
        btnDeleteAd.setVisibility(View.GONE);
        resetForm();
    }

    private void resetForm() {
        etGearName.setText("");
        etGearPrice.setText("");
        etGearLat.setText("");
        etGearLng.setText("");
        etGearContact.setText("");
        ivGearPreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of the Admin Panel?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Logout", (dialog, which) -> {
                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    sharedPref.edit().clear().apply();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
