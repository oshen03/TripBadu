package com.example.tripbadu;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MembersListFragment extends Fragment {

    public interface OnMemberActionListener {
        void onMemberDeleted();
        void onMemberSelected(User user);
    }

    private RecyclerView rvMembers;
    private EditText etSearch;
    private TextView tvEmpty;
    private ManageMembersAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private OnMemberActionListener listener;

    private static final String KEY_SEARCH_QUERY = "search_query";
    private String currentQuery = "";

    public MembersListFragment() {
        // Required empty public constructor
    }

    public static MembersListFragment newInstance() {
        return new MembersListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMemberActionListener) {
            listener = (OnMemberActionListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMembers = view.findViewById(R.id.rvMembersFragment);
        etSearch = view.findViewById(R.id.etSearchMember);
        tvEmpty = view.findViewById(R.id.tvEmptyMembers);

        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ManageMembersAdapter(requireContext(), filteredList);
        rvMembers.setAdapter(adapter);

        loadMembers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                filterMembers(currentQuery);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (!currentQuery.isEmpty()) {
            etSearch.setText(currentQuery);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMembers();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, currentQuery);
    }

    public void loadMembers() {
        if (!isAdded()) return;
        if (dbHelper == null && getContext() != null) {
            dbHelper = new DatabaseHelper(requireContext());
        }
        if (dbHelper == null) return;
        userList.clear();
        Cursor cursor = dbHelper.getAllUsers();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));
                    userList.add(new User(id, name, email, role));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        filterMembers(currentQuery);
    }

    public void filterMembers(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lower = query.toLowerCase().trim();
            for (User u : userList) {
                if (u.getName().toLowerCase().contains(lower) || u.getEmail().toLowerCase().contains(lower) || u.getRole().toLowerCase().contains(lower)) {
                    filteredList.add(u);
                }
            }
        }

        if (adapter == null && getContext() != null) {
            adapter = new ManageMembersAdapter(requireContext(), filteredList);
        }
        if (rvMembers != null && rvMembers.getAdapter() == null && adapter != null) {
            rvMembers.setAdapter(adapter);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
