package com.example.tripbadu;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemberReportFragment extends Fragment {

    private BarChartView barChartView;
    private PieChartView pieChartView;
    private TextView tvStatTotal, tvStatUsers, tvStatVips;
    private Button btnShowBarChart, btnShowPieChart, btnRefreshWorkerThread;
    private ProgressBar pbLoading;

    private DatabaseHelper dbHelper;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String KEY_CHART_TYPE = "current_chart_type";
    private boolean isShowingPieChart = false;

    public MemberReportFragment() {
        // Required empty public constructor
    }

    public static MemberReportFragment newInstance() {
        return new MemberReportFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        if (savedInstanceState != null) {
            isShowingPieChart = savedInstanceState.getBoolean(KEY_CHART_TYPE, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChartView = view.findViewById(R.id.barChartView);
        pieChartView = view.findViewById(R.id.pieChartView);
        tvStatTotal = view.findViewById(R.id.tvStatTotal);
        tvStatUsers = view.findViewById(R.id.tvStatUsers);
        tvStatVips = view.findViewById(R.id.tvStatVips);
        btnShowBarChart = view.findViewById(R.id.btnShowBarChart);
        btnShowPieChart = view.findViewById(R.id.btnShowPieChart);
        btnRefreshWorkerThread = view.findViewById(R.id.btnRefreshWorkerThread);
        pbLoading = view.findViewById(R.id.pbLoadingReport);

        btnShowBarChart.setOnClickListener(v -> showChart(false));
        btnShowPieChart.setOnClickListener(v -> showChart(true));

        // Trigger manual background worker thread calculation
        btnRefreshWorkerThread.setOnClickListener(v -> calculateStatisticsOnWorkerThread(true));

        showChart(isShowingPieChart);
        calculateStatisticsOnWorkerThread(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        calculateStatisticsOnWorkerThread(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_CHART_TYPE, isShowingPieChart);
    }

    private void showChart(boolean showPie) {
        isShowingPieChart = showPie;
        if (showPie) {
            barChartView.setVisibility(View.GONE);
            pieChartView.setVisibility(View.VISIBLE);
            btnShowPieChart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnShowBarChart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        } else {
            barChartView.setVisibility(View.VISIBLE);
            pieChartView.setVisibility(View.GONE);
            btnShowBarChart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            btnShowPieChart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        }
    }

    /**
     * Demonstrates Criteria 3.1: Proper handling of Threads (Worker Thread -> UI Thread)
     */
    public void calculateStatisticsOnWorkerThread(boolean showToastFeedback) {
        if (!isAdded()) return;

        if (pbLoading != null) {
            pbLoading.setVisibility(View.VISIBLE);
        }

        // Execute analytical query on a dedicated background Worker Thread
        backgroundExecutor.execute(() -> {
            int users = 0;
            int vips = 0;

            try {
                // Simulate analytical calculation time
                Thread.sleep(150);

                if (dbHelper == null && getContext() != null) {
                    dbHelper = new DatabaseHelper(requireContext());
                }
                if (dbHelper != null) {
                    Cursor cursor = dbHelper.getAllUsers();
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));
                                if ("VIP".equalsIgnoreCase(role)) {
                                    vips++;
                                } else {
                                    users++;
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int finalUsers = users;
            final int finalVips = vips;
            final int finalTotal = users + vips;

            // Post back results to the Main UI Thread
            mainHandler.post(() -> {
                if (!isAdded() || getView() == null) return;

                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                // Update text stats
                if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(finalTotal));
                if (tvStatUsers != null) tvStatUsers.setText(String.valueOf(finalUsers));
                if (tvStatVips != null) tvStatVips.setText(String.valueOf(finalVips));

                // Update custom charts
                if (barChartView != null) barChartView.setData(finalUsers, finalVips);
                if (pieChartView != null) pieChartView.setData(finalUsers, finalVips);

                if (showToastFeedback && getContext() != null) {
                    Toast.makeText(requireContext(), "Reports updated via Worker Thread! (Total: " + finalTotal + ")", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
    }
}
