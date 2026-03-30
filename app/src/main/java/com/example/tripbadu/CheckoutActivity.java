package com.example.tripbadu;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutActivity extends AppCompatActivity {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private PaymentsClient paymentsClient;
    private RelativeLayout googlePayButton;
    private Button btnPay;
    private TextView tvOrderTotal;
    private ProgressBar progressBar;
    private double totalAmount;
    private DatabaseHelper dbHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        googlePayButton = findViewById(R.id.googlePayButton);
        btnPay = findViewById(R.id.btnPay);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        progressBar = findViewById(R.id.progressBar);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        tvOrderTotal.setText("Total: LKR " + totalAmount);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/") // Change to your server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Initialize Google Pay
        paymentsClient = PaymentsUtil.createPaymentsClient(this);
        possiblyShowGooglePayButton();

        googlePayButton.setOnClickListener(v -> requestPayment());
        
        // Fallback or alternative payment method
        btnPay.setOnClickListener(v -> {
            Toast.makeText(this, "Standard payment not implemented. Please use Google Pay.", Toast.LENGTH_SHORT).show();
        });
    }

    private void possiblyShowGooglePayButton() {
        final IsReadyToPayRequest request = PaymentsUtil.getIsReadyToPayRequest();
        if (request == null) return;

        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this, completedTask -> {
            if (completedTask.isSuccessful() && completedTask.getResult()) {
                setGooglePayAvailable(true);
            } else {
                setGooglePayAvailable(false);
                Log.w("isReadyToPay failed", completedTask.getException());
            }
        });
    }

    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
            btnPay.setVisibility(View.GONE);
        } else {
            googlePayButton.setVisibility(View.GONE);
            btnPay.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Google Pay is not available on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private void requestPayment() {
        // Disabling the button to prevent multiple clicks.
        googlePayButton.setClickable(false);

        // Converting amount to cents for Stripe
        long amountCents = Math.round(totalAmount * 100);

        PaymentDataRequest request = PaymentsUtil.getPaymentDataRequest(amountCents);

        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    this,
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    handlePaymentSuccess(paymentData);
                    break;
                case Activity.RESULT_CANCELED:
                    // Nothing to here
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    handleError(status.getStatusCode());
                    break;
            }
            googlePayButton.setClickable(true);
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();
        if (paymentInfo == null) return;

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            String token = paymentMethodData.getJSONObject("tokenizationData").getString("token");

            // Show loading spinner
            progressBar.setVisibility(View.VISIBLE);
            googlePayButton.setVisibility(View.GONE);

            // Send token to backend
            long amountCents = Math.round(totalAmount * 100);
            ChargeRequest chargeRequest = new ChargeRequest(token, amountCents);

            apiService.charge(chargeRequest).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        processCheckoutOnServer();
                    } else {
                        googlePayButton.setVisibility(View.VISIBLE);
                        Toast.makeText(CheckoutActivity.this, "Payment failed on server.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    googlePayButton.setVisibility(View.VISIBLE);
                    Toast.makeText(CheckoutActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error with JSON: " + e.toString());
        }
    }

    private void handleError(int statusCode) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
        Toast.makeText(this, "An error occurred with Google Pay.", Toast.LENGTH_SHORT).show();
    }

    private void processCheckoutOnServer() {
        List<Gear> items = new ArrayList<>();
        Cursor cursor = dbHelper.getCartItems();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(new Gear(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_IMAGE))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }

        Order order = new Order("John Doe", "john@example.com", items, totalAmount);

        apiService.processCheckout(order).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Toast.makeText(CheckoutActivity.this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
                dbHelper.clearCart();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Order processing failed.", Toast.LENGTH_SHORT).show();
                dbHelper.clearCart();
                finish();
            }
        });
    }
}
