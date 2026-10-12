package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Check for admin hardcoded credentials
                    if (email.equals("admin") && password.equals("admin")) {
                        saveSession(-1, "Admin", "Administrator", "admin");
                        Toast.makeText(LoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        Cursor cursor = dbHelper.getUser(email, password);
                        if (cursor != null && cursor.moveToFirst()) {
                            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                            cursor.close();
                            
                            saveSession(id, role, name, userEmail);
                            proceedToDashboard(role, "Login Successful");
                        } else {
                            if (cursor != null) cursor.close();
                            // Check Firebase Cloud Database as fallback
                            checkFirebaseLogin(email, password);
                        }
                    }
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void saveSession(int id, String role, String name, String email) {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("userId", id);
        editor.putString("role", role);
        editor.putString("userName", name);
        editor.putString("email", email);
        editor.apply();
    }

    private void checkFirebaseLogin(String email, String password) {
        String inputHash = DatabaseHelper.hashPassword(password);
        FirebaseHelper.getInstance().getUser(email, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CloudUser cloudUser = snapshot.getValue(CloudUser.class);
                    if (cloudUser != null && cloudUser.getPasswordHash() != null &&
                            (cloudUser.getPasswordHash().equals(inputHash) || cloudUser.getPasswordHash().equals(password))) {
                        // Cache user to local SQLite
                        dbHelper.insertOrUpdateUser(cloudUser.getEmail(), cloudUser.getPasswordHash(), cloudUser.getName(), cloudUser.getRole());
                        saveSession(0, cloudUser.getRole(), cloudUser.getName(), cloudUser.getEmail());
                        proceedToDashboard(cloudUser.getRole(), "Cloud Login Successful");
                        return;
                    }
                }
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Login error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToDashboard(String role, String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        if ("VIP".equalsIgnoreCase(role)) {
            startActivity(new Intent(LoginActivity.this, VipDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }
        finish();
    }
}
