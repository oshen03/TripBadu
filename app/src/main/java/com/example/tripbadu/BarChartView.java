package com.example.tripbadu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class BarChartView extends View {

    private int userCount = 0;
    private int vipCount = 0;

    private Paint barPaint;
    private Paint textPaint;
    private Paint axisPaint;
    private Paint legendPaint;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(4f);

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setTextSize(28f);
    }

    public void setData(int userCount, int vipCount) {
        this.userCount = userCount;
        this.vipCount = vipCount;
        invalidate(); // Trigger re-draw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        float bottomY = height - 120f;
        float topY = 60f;
        float availableHeight = bottomY - topY;

        // Draw baseline axis
        canvas.drawLine(60f, bottomY, width - 60f, bottomY, axisPaint);

        int maxVal = Math.max(1, Math.max(userCount, vipCount));
        float barWidth = Math.min(180f, (width - 240f) / 3f);

        // Bar 1: Regular Users
        float userBarHeight = (userCount / (float) maxVal) * (availableHeight - 60f);
        float userLeft = width * 0.28f - barWidth / 2f;
        float userRight = userLeft + barWidth;
        float userTop = bottomY - userBarHeight;

        barPaint.setColor(Color.parseColor("#1976D2")); // Blue
        RectF userRect = new RectF(userLeft, userTop, userRight, bottomY);
        canvas.drawRoundRect(userRect, 16f, 16f, barPaint);

        // Value on top of User bar
        canvas.drawText(String.valueOf(userCount), (userLeft + userRight) / 2f, Math.max(topY + 30f, userTop - 12f), textPaint);
        // Label under User bar
        canvas.drawText("Users", (userLeft + userRight) / 2f, bottomY + 45f, textPaint);

        // Bar 2: VIP Members
        float vipBarHeight = (vipCount / (float) maxVal) * (availableHeight - 60f);
        float vipLeft = width * 0.72f - barWidth / 2f;
        float vipRight = vipLeft + barWidth;
        float vipTop = bottomY - vipBarHeight;

        barPaint.setColor(Color.parseColor("#FFA000")); // Amber
        RectF vipRect = new RectF(vipLeft, vipTop, vipRight, bottomY);
        canvas.drawRoundRect(vipRect, 16f, 16f, barPaint);

        // Value on top of VIP bar
        canvas.drawText(String.valueOf(vipCount), (vipLeft + vipRight) / 2f, Math.max(topY + 30f, vipTop - 12f), textPaint);
        // Label under VIP bar
        canvas.drawText("VIPs", (vipLeft + vipRight) / 2f, bottomY + 45f, textPaint);

        // Summary footer
        int total = userCount + vipCount;
        legendPaint.setColor(Color.DKGRAY);
        legendPaint.setTextAlign(Paint.Align.CENTER);
        String summary = "Total Registered Members: " + total;
        canvas.drawText(summary, width / 2f, height - 20f, legendPaint);
    }
}
