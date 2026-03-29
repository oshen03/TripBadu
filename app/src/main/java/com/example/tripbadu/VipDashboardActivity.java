package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VipDashboardActivity extends AppCompatActivity {

    private ImageView ivAdPreview;
    private EditText etAdName, etAdDesc, etAdPrice;
    private Button btnSelectImage, btnSubmitAd, btnGoHome;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_dashboard);

        ivAdPreview = findViewById(R.id.ivAdPreview);
        etAdName = findViewById(R.id.etAdName);
        etAdDesc = findViewById(R.id.etAdDesc);
        etAdPrice = findViewById(R.id.etAdPrice);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmitAd = findViewById(R.id.btnSubmitAd);
        btnGoHome = findViewById(R.id.btnGoHome);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivAdPreview.setImageURI(selectedImageUri);
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSubmitAd.setOnClickListener(v -> uploadAd());

        btnGoHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void uploadAd() {
        String name = etAdName.getText().toString().trim();
        String desc = etAdDesc.getText().toString().trim();
        String price = etAdPrice.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty() || price.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = pref.getInt("userId", -1);

        try {
            File file = getFileFromUri(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            RequestBody userIdPart = RequestBody.create(MultipartBody.FORM, String.valueOf(userId));
            RequestBody namePart = RequestBody.create(MultipartBody.FORM, name);
            RequestBody descPart = RequestBody.create(MultipartBody.FORM, desc);
            RequestBody pricePart = RequestBody.create(MultipartBody.FORM, price);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            apiService.uploadAd(userIdPart, namePart, descPart, pricePart, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(VipDashboardActivity.this, "Ad Submitted! Waiting for Approval", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(VipDashboardActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(VipDashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        return tempFile;
    }
}
