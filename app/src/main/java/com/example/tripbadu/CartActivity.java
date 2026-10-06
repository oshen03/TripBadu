package com.example.tripbadu;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private List<Gear> cartList = new ArrayList<>();
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private DatabaseHelper dbHelper;
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DatabaseHelper(this);
        rvCart = findViewById(R.id.rvCart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        loadCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("TOTAL_AMOUNT", total);
                startActivity(intent);
            }
        });
    }

    private void loadCart() {
        cartList.clear();
        total = 0;
        Cursor cursor = dbHelper.getCartItems();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROD_IMAGE));
                
                cartList.add(new Gear(id, name, price, image));
                total += price;
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new CartAdapter(this, cartList, (item, position) -> {
            dbHelper.deleteCartItem(item.getId());
            cartList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, cartList.size());
            recalculateTotal();
            Toast.makeText(CartActivity.this, item.getName() + " removed from cart", Toast.LENGTH_SHORT).show();
        });
        rvCart.setAdapter(adapter);
        tvTotalAmount.setText("Total: LKR " + total);
    }

    private void recalculateTotal() {
        total = 0;
        for (Gear item : cartList) {
            total += item.getPrice();
        }
        tvTotalAmount.setText("Total: LKR " + total);
    }
}
