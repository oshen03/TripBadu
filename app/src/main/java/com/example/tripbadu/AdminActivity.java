package com.example.tripbadu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private EditText etGearName, etGearPrice, etGearImage;
    private Button btnAddGear, btnViewMain;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        etGearName = findViewById(R.id.etGearName);
        etGearPrice = findViewById(R.id.etGearPrice);
        etGearImage = findViewById(R.id.etGearImage);
        btnAddGear = findViewById(R.id.btnAddGear);
        btnViewMain = findViewById(R.id.btnViewMain);

        btnAddGear.setOnClickListener(v -> {
            String name = etGearName.getText().toString().trim();
            String priceStr = etGearPrice.getText().toString().trim();
            String image = etGearImage.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || image.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                double price = Double.parseDouble(priceStr);
                dbHelper.addGear(name, price, image);
                Toast.makeText(this, "Gear Added Successfully", Toast.LENGTH_SHORT).show();
                
                // Clear fields
                etGearName.setText("");
                etGearPrice.setText("");
                etGearImage.setText("");
            }
        });

        btnViewMain.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, HomeActivity.class));
            finish();
        });
    }
}
