package com.example.tripbadu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        // Criteria 3.2: onClick on list item (Fragment-to-Activity communication)
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MembersListFragment.OnMemberActionListener) {
                ((MembersListFragment.OnMemberActionListener) context).onMemberSelected(user);
            }
        });

        // Criteria 3.2: onLongClick on list item
        holder.itemView.setOnLongClickListener(v -> {
            Toast.makeText(context, "Long-pressed member: " + user.getName() + " (" + user.getRole() + ")", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Criteria 2.3: AlertDialog for delete confirmation
        holder.btnDelete.setOnClickListener(v -> {
            if ("admin".equalsIgnoreCase(user.getEmail())) {
                Toast.makeText(context, "Cannot delete administrator account", Toast.LENGTH_SHORT).show();
                return;
            }

            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(context)
                    .setTitle("Delete Member")
                    .setMessage("Are you sure you want to remove " + user.getName() + " (" + user.getEmail() + ")?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteUser(user.getId());
                        Toast.makeText(context, "Member removed successfully", Toast.LENGTH_SHORT).show();
                        if (adapterPos < userList.size()) {
                            userList.remove(adapterPos);
                            notifyItemRemoved(adapterPos);
                            notifyItemRangeChanged(adapterPos, userList.size());
                        }

                        // Notify host activity so MemberReportFragment updates its charts!
                        if (context instanceof MembersListFragment.OnMemberActionListener) {
                            ((MembersListFragment.OnMemberActionListener) context).onMemberDeleted();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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
