package com.example.tripbadu;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etCardNumber, etExpiry, etCVV;
    private Button btnPay;
    private TextView tvOrderTotal;
    private double totalAmount;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);
        btnPay = findViewById(R.id.btnPay);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        tvOrderTotal.setText("Total: LKR " + totalAmount);

        btnPay.setOnClickListener(v -> {
            String cardNumber = etCardNumber.getText().toString();
            if (validateCard(cardNumber)) {
                processPayment();
            } else {
                Toast.makeText(this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateCard(String cardNumber) {
        if (cardNumber.length() != 16) return false;
        
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private void processPayment() {
        List<Gear> items = new ArrayList<>();
        Cursor cursor = dbHelper.getCartItems();
        if (cursor.moveToFirst()) {
            do {
                items.add(new Gear(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_IMAGE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Order order = new Order("Guest User", "guest@example.com", items, totalAmount);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.processCheckout(order).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(CheckoutActivity.this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
                dbHelper.clearCart();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Payment Simulated Locally. Server Unreachable.", Toast.LENGTH_SHORT).show();
                dbHelper.clearCart();
                finish();
            }
        });
    }
}
