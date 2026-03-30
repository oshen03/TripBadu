package com.example.tripbadu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ManageMembersAdapter extends RecyclerView.Adapter<ManageMembersAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private DatabaseHelper dbHelper;

    public ManageMembersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole());

        holder.btnDelete.setOnClickListener(v -> {
            if (user.getEmail().equals("admin")) {
                Toast.makeText(context, "Cannot delete admin", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.deleteUser(user.getId());
            Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
            userList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, userList.size());
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvRole = itemView.findViewById(R.id.tvMemberRole);
            btnDelete = itemView.findViewById(R.id.btnDeleteMember);
        }
    }
}
