package com.example.tripbadu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GearAdapter extends RecyclerView.Adapter<GearAdapter.GearViewHolder> {

    private Context context;
    private List<Gear> gearList;
    private DatabaseHelper dbHelper;

    public GearAdapter(Context context, List<Gear> gearList) {
        this.context = context;
        this.gearList = gearList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public GearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gear, parent, false);
        return new GearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GearViewHolder holder, int position) {
        Gear gear = gearList.get(position);
        holder.tvName.setText(gear.getName());
        holder.tvPrice.setText("LKR " + gear.getPrice());
        
        // In a real app, use Glide or Picasso to load images from URL
        // holder.ivGear.setImageResource(R.drawable.gear_placeholder); 

        holder.btnAddToCart.setOnClickListener(v -> {
            dbHelper.addToCart(gear.getName(), gear.getPrice(), gear.getImage());
            Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return gearList.size();
    }

    public static class GearViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivGear;
        Button btnAddToCart;

        public GearViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGearName);
            tvPrice = itemView.findViewById(R.id.tvGearPrice);
            ivGear = itemView.findViewById(R.id.ivGear);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
