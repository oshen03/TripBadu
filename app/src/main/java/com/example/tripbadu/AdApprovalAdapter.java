package com.example.tripbadu;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class AdApprovalAdapter extends RecyclerView.Adapter<AdApprovalAdapter.ViewHolder> {

    private Context context;
    private List<Gear> pendingAds;
    private DatabaseHelper dbHelper;

    public AdApprovalAdapter(Context context, List<Gear> pendingAds) {
        this.context = context;
        this.pendingAds = pendingAds;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_ad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gear gear = pendingAds.get(holder.getAdapterPosition());
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
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnApprove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                dbHelper.approveGear(gear.getId());
                Toast.makeText(context, "Ad Approved", Toast.LENGTH_SHORT).show();
                pendingAds.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                dbHelper.deleteGear(gear.getId());
                Toast.makeText(context, "Ad Rejected", Toast.LENGTH_SHORT).show();
                pendingAds.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingAds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImage;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPendingName);
            tvPrice = itemView.findViewById(R.id.tvPendingPrice);
            ivImage = itemView.findViewById(R.id.ivPendingImage);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
