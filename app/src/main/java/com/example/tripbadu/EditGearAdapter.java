package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class EditGearAdapter extends RecyclerView.Adapter<EditGearAdapter.ViewHolder> {

    public interface OnGearActionListener {
        void onGearDeleted(Gear gear);
    }

    private Context context;
    private List<Gear> approvedGear;
    private DatabaseHelper dbHelper;
    private OnGearActionListener actionListener;

    public EditGearAdapter(Context context, List<Gear> approvedGear) {
        this(context, approvedGear, null);
    }

    public EditGearAdapter(Context context, List<Gear> approvedGear, OnGearActionListener actionListener) {
        this.context = context;
        this.approvedGear = approvedGear;
        this.dbHelper = new DatabaseHelper(context);
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_gear, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gear gear = approvedGear.get(position);
        holder.tvName.setText(gear.getName());
        holder.tvPrice.setText("LKR " + gear.getPrice());

        String owner = (gear.getOwnerEmail() != null && !gear.getOwnerEmail().isEmpty()) ? gear.getOwnerEmail() : "Admin";
        holder.tvOwner.setText("By: " + owner);

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

        // Edit button - opens AdminActivity with gear details prefilled
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminActivity.class);
            intent.putExtra("edit_id", gear.getId());
            intent.putExtra("edit_name", gear.getName());
            intent.putExtra("edit_price", gear.getPrice());
            intent.putExtra("edit_image", gear.getImage());
            intent.putExtra("edit_lat", gear.getLatitude());
            intent.putExtra("edit_lng", gear.getLongitude());
            intent.putExtra("edit_contact", gear.getContact());
            context.startActivity(intent);
        });

        // Delete button - displays confirmation dialog (Criteria 2.3) and removes ad
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION || currentPos >= approvedGear.size()) {
                return;
            }
            Gear currentGear = approvedGear.get(currentPos);

            new AlertDialog.Builder(context)
                    .setTitle("Delete Published Ad")
                    .setMessage("Are you sure you want to permanently delete \"" + currentGear.getName() + "\" from the marketplace?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < approvedGear.size()) {
                            Gear toDelete = approvedGear.get(pos);

                            // Delete from local SQLite
                            dbHelper.deleteGear(toDelete.getId());

                            // Delete from Firebase Cloud
                            FirebaseHelper.getInstance().deleteGear(toDelete.getId());

                            // Notify owner if it was submitted by a member
                            if (toDelete.getOwnerEmail() != null && !toDelete.getOwnerEmail().isEmpty() && !toDelete.getOwnerEmail().equalsIgnoreCase("admin")) {
                                dbHelper.addNotification(toDelete.getOwnerEmail(), "Your published ad for '" + toDelete.getName() + "' was deleted by an administrator.");
                            }

                            approvedGear.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, approvedGear.size());

                            Toast.makeText(context, "Ad \"" + toDelete.getName() + "\" deleted successfully", Toast.LENGTH_SHORT).show();

                            if (actionListener != null) {
                                actionListener.onGearDeleted(toDelete);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return approvedGear.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvOwner;
        ImageView ivImage, btnDelete;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEditName);
            tvPrice = itemView.findViewById(R.id.tvEditPrice);
            tvOwner = itemView.findViewById(R.id.tvEditOwner);
            ivImage = itemView.findViewById(R.id.ivEditImage);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteGear);
        }
    }
}
