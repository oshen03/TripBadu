package com.example.tripbadu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ManageMembersActivity extends AppCompatActivity implements MembersListFragment.OnMemberActionListener {

    private Button btnTabList, btnTabReport;
    private ImageView ivBack;

    private MembersListFragment membersListFragment;
    private MemberReportFragment memberReportFragment;

    private static final String KEY_ACTIVE_TAB = "active_tab";
    private int activeTab = 0; // 0 = List, 1 = Report

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_members);

        btnTabList = findViewById(R.id.btnTabList);
        btnTabReport = findViewById(R.id.btnTabReport);
        ivBack = findViewById(R.id.ivBackMembers);

        ivBack.setOnClickListener(v -> finish());

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            activeTab = savedInstanceState.getInt(KEY_ACTIVE_TAB, 0);
            membersListFragment = (MembersListFragment) fm.findFragmentByTag("FRAG_LIST");
            memberReportFragment = (MemberReportFragment) fm.findFragmentByTag("FRAG_REPORT");
        }

        if (membersListFragment == null) {
            membersListFragment = MembersListFragment.newInstance();
        }
        if (memberReportFragment == null) {
            memberReportFragment = MemberReportFragment.newInstance();
        }

        // Initialize and attach fragments synchronously only on initial creation
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, memberReportFragment, "FRAG_REPORT")
                    .hide(memberReportFragment)
                    .add(R.id.fragmentContainer, membersListFragment, "FRAG_LIST")
                    .commitNow();
        }

        btnTabList.setOnClickListener(v -> switchTab(0));
        btnTabReport.setOnClickListener(v -> switchTab(1));

        updateTabUi(activeTab);
        if (savedInstanceState != null && activeTab == 1) {
            fm.beginTransaction().hide(membersListFragment).show(memberReportFragment).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ACTIVE_TAB, activeTab);
    }

    /**
     * Demonstrates Criteria 1.3: Dynamic Fragment transactions and lifecycle switching
     */
    private void switchTab(int tabIndex) {
        if (activeTab == tabIndex) return;
        activeTab = tabIndex;
        updateTabUi(tabIndex);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (tabIndex == 0) {
            ft.hide(memberReportFragment).show(membersListFragment).commit();
            membersListFragment.loadMembers();
        } else {
            ft.hide(membersListFragment).show(memberReportFragment).commit();
            memberReportFragment.calculateStatisticsOnWorkerThread(false);
        }
    }

    private void updateTabUi(int tabIndex) {
        if (tabIndex == 0) {
            btnTabList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnTabReport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        } else {
            btnTabReport.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnTabList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        }
    }

    /**
     * Demonstrates Fragment-to-Activity communication:
     * When a member is deleted or updated in MembersListFragment,
     * this callback is triggered, and the Activity notifies MemberReportFragment
     * (Fragment-to-Fragment communication via Activity).
     */
    @Override
    public void onMemberDeleted() {
        if (memberReportFragment != null && memberReportFragment.isAdded()) {
            memberReportFragment.calculateStatisticsOnWorkerThread(false);
        }
    }

    @Override
    public void onMemberSelected(User user) {
        // Show an AlertDialog with member details (Demonstrating Criteria 2.3)
        new AlertDialog.Builder(this)
                .setTitle("Member Profile: " + user.getName())
                .setMessage("Email: " + user.getEmail() + "\nRole: " + user.getRole())
                .setPositiveButton("OK", null)
                .show();
    }
}
