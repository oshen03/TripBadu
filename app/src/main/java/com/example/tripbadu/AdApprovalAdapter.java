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
import com.bumptech.glide.Glide;
import java.util.List;

public class AdApprovalAdapter extends RecyclerView.Adapter<AdApprovalAdapter.ViewHolder> {

    public interface OnAdActionListener {
        void onActionCompleted();
    }

    private Context context;
    private List<Gear> pendingAds;
    private DatabaseHelper dbHelper;
    private OnAdActionListener actionListener;

    public AdApprovalAdapter(Context context, List<Gear> pendingAds, OnAdActionListener actionListener) {
        this.context = context;
        this.pendingAds = pendingAds;
        this.dbHelper = new DatabaseHelper(context);
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_ad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gear gear = pendingAds.get(position);
        holder.tvName.setText(gear.getName());
        holder.tvPrice.setText("LKR " + gear.getPrice());

        String owner = (gear.getOwnerEmail() != null && !gear.getOwnerEmail().isEmpty()) ? gear.getOwnerEmail() : "VIP Member";
        holder.tvOwner.setText("Submitted by: " + owner);

        String contact = (gear.getContact() != null && !gear.getContact().isEmpty()) ? gear.getContact() : "N/A";
        holder.tvContact.setText("Contact: " + contact);

        holder.tvLocation.setText("Location: " + gear.getLatitude() + ", " + gear.getLongitude());

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
            if (currentPos != RecyclerView.NO_POSITION && currentPos < pendingAds.size()) {
                Gear currentGear = pendingAds.get(currentPos);
                dbHelper.approveGear(currentGear.getId());
                Toast.makeText(context, "Ad '" + currentGear.getName() + "' Approved", Toast.LENGTH_SHORT).show();
                pendingAds.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, pendingAds.size());
                if (actionListener != null) {
                    actionListener.onActionCompleted();
                }
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && currentPos < pendingAds.size()) {
                Gear currentGear = pendingAds.get(currentPos);
                dbHelper.rejectGear(currentGear.getId());
                Toast.makeText(context, "Ad '" + currentGear.getName() + "' Rejected", Toast.LENGTH_SHORT).show();
                pendingAds.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, pendingAds.size());
                if (actionListener != null) {
                    actionListener.onActionCompleted();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingAds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvOwner, tvContact, tvLocation;
        ImageView ivImage;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPendingName);
            tvPrice = itemView.findViewById(R.id.tvPendingPrice);
            tvOwner = itemView.findViewById(R.id.tvPendingOwner);
            tvContact = itemView.findViewById(R.id.tvPendingContact);
            tvLocation = itemView.findViewById(R.id.tvPendingLocation);
            ivImage = itemView.findViewById(R.id.ivPendingImage);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
