package com.example.tripbadu;

import android.content.Context;
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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemDeleteListener {
        void onDelete(Gear item, int position);
    }

    private Context context;
    private List<Gear> cartList;
    private OnCartItemDeleteListener deleteListener;

    public CartAdapter(Context context, List<Gear> cartList, OnCartItemDeleteListener deleteListener) {
        this.context = context;
        this.cartList = cartList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Gear item = cartList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("LKR " + item.getPrice());

        if (item.getImage() != null && !item.getImage().isEmpty()) {
            String img = item.getImage();
            if (!img.startsWith("http://") && !img.startsWith("https://") && !img.startsWith("content://") && !img.startsWith("file://")) {
                img = "http://192.168.8.113:3000/images/" + img;
            }
            Glide.with(context)
                    .load(img)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivItem);
        } else {
            holder.ivItem.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivItem;
        Button btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            ivItem = itemView.findViewById(R.id.ivCartItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteFromCart);
        }
    }
}
