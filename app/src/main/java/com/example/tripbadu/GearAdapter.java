package com.example.tripbadu;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class GearAdapter extends RecyclerView.Adapter<GearAdapter.GearViewHolder> {

    private Context context;
    private List<Gear> gearList;
    private List<Gear> gearListFull; // For filtering
    private DatabaseHelper dbHelper;

    public GearAdapter(Context context, List<Gear> gearList) {
        this.context = context;
        this.gearList = gearList;
        this.gearListFull = new ArrayList<>(gearList);
        this.dbHelper = new DatabaseHelper(context);
    }

    public void updateList(List<Gear> newList) {
        this.gearList = newList;
        this.gearListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        List<Gear> filteredList = new ArrayList<>();
        for (Gear item : gearListFull) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        this.gearList = filteredList;
        notifyDataSetChanged();
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
        
        if (gear.getImage() != null && !gear.getImage().isEmpty()) {
            String img = gear.getImage();
            if (!img.startsWith("http://") && !img.startsWith("https://") && !img.startsWith("content://") && !img.startsWith("file://")) {
                img = "http://192.168.8.113:3000/images/" + img;
            }
            Glide.with(context)
                    .load(img)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivGear);
        } else {
            holder.ivGear.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnAddToCart.setOnClickListener(v -> {
            // Scale-pulse feedback on the button itself
            holder.btnAddToCart.animate()
                    .scaleX(0.92f).scaleY(0.92f).setDuration(80)
                    .withEndAction(() ->
                            holder.btnAddToCart.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    ).start();

            dbHelper.addToCart(gear.getName(), gear.getPrice(), gear.getImage());
            Toast.makeText(context, gear.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });

        // Fade-in + slide-up entrance animation for each card
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        holder.itemView.startAnimation(anim);
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
