package com.example.tripbadu;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class VipAdsAdapter extends RecyclerView.Adapter<VipAdsAdapter.ViewHolder> {

    private Context context;
    private List<Gear> myAds;
    private DatabaseHelper dbHelper;
    private OnAdDeletedListener listener;

    public interface OnAdDeletedListener {
        void onAdDeleted();
    }

    public VipAdsAdapter(Context context, List<Gear> myAds, OnAdDeletedListener listener) {
        this.context = context;
        this.myAds = myAds;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vip_ad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gear gear = myAds.get(position);
        holder.tvName.setText(gear.getName());
        holder.tvPrice.setText("LKR " + gear.getPrice());
        holder.tvStatus.setText("Status: " + gear.getStatus());
        
        if (gear.getStatus().equalsIgnoreCase("pending")) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else if (gear.getStatus().equalsIgnoreCase("rejected")) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        
        if (gear.getImage() != null && !gear.getImage().isEmpty()) {
            String img = gear.getImage();
            if (!img.startsWith("http://") && !img.startsWith("https://") && !img.startsWith("content://") && !img.startsWith("file://")) {
                img = "http://192.168.8.113:3000/images/" + img;
            }
            Glide.with(context)
                    .load(img)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.ivDelete.setOnClickListener(v -> {
            dbHelper.deleteGear(gear.getId());
            Toast.makeText(context, "Ad Deleted", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onAdDeleted();
            }
        });
    }

    @Override
    public int getItemCount() {
        return myAds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStatus;
        ImageView ivImage, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVipAdName);
            tvPrice = itemView.findViewById(R.id.tvVipAdPrice);
            tvStatus = itemView.findViewById(R.id.tvVipAdStatus);
            ivImage = itemView.findViewById(R.id.ivVipAdImage);
            ivDelete = itemView.findViewById(R.id.ivDeleteAd);
        }
    }
}
