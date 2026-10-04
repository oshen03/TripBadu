package com.example.tripbadu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class EditGearAdapter extends RecyclerView.Adapter<EditGearAdapter.ViewHolder> {

    private Context context;
    private List<Gear> approvedGear;

    public EditGearAdapter(Context context, List<Gear> approvedGear) {
        this.context = context;
        this.approvedGear = approvedGear;
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
    }

    @Override
    public int getItemCount() {
        return approvedGear.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImage;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEditName);
            tvPrice = itemView.findViewById(R.id.tvEditPrice);
            ivImage = itemView.findViewById(R.id.ivEditImage);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
        }
    }
}
