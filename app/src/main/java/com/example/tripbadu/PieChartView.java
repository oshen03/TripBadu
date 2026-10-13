package com.example.tripbadu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class PieChartView extends View {

    private int userCount = 0;
    private int vipCount = 0;

    private Paint userPaint;
    private Paint vipPaint;
    private Paint textPaint;
    private Paint legendPaint;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        userPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPaint.setStyle(Paint.Style.FILL);
        userPaint.setColor(Color.parseColor("#1976D2")); // Blue

        vipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vipPaint.setStyle(Paint.Style.FILL);
        vipPaint.setColor(Color.parseColor("#FFA000")); // Amber

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setTextSize(26f);
        legendPaint.setColor(Color.DKGRAY);
    }

    public void setData(int userCount, int vipCount) {
        this.userCount = userCount;
        this.vipCount = vipCount;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int total = userCount + vipCount;
        if (total == 0) {
            // Draw empty placeholder
            Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            emptyPaint.setColor(Color.LTGRAY);
            emptyPaint.setStyle(Paint.Style.STROKE);
            emptyPaint.setStrokeWidth(6f);
            canvas.drawCircle(width / 2f, height / 2f - 40f, Math.min(width, height) / 3f, emptyPaint);
            legendPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No member data available", width / 2f, height / 2f - 30f, legendPaint);
            return;
        }

        float userAngle = (userCount / (float) total) * 360f;
        float vipAngle = 360f - userAngle;

        float radius = Math.min(width, height) * 0.35f;
        float cx = width / 2f;
        float cy = height * 0.42f;

        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw User slice
        if (userCount > 0) {
            canvas.drawArc(oval, -90f, userAngle, true, userPaint);
        }

        // Draw VIP slice
        if (vipCount > 0) {
            canvas.drawArc(oval, -90f + userAngle, vipAngle, true, vipPaint);
        }

        // Legends below chart
        float legendY = cy + radius + 50f;
        float boxSize = 24f;

        // User legend
        float userPct = (userCount * 100f) / total;
        canvas.drawRect(cx - 160f, legendY - boxSize + 6f, cx - 160f + boxSize, legendY + 6f, userPaint);
        legendPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("Users: %d (%.1f%%)", userCount, userPct), cx - 120f, legendY, legendPaint);

        // VIP legend
        float vipPct = (vipCount * 100f) / total;
        float vipLegendY = legendY + 45f;
        canvas.drawRect(cx - 160f, vipLegendY - boxSize + 6f, cx - 160f + boxSize, vipLegendY + 6f, vipPaint);
        canvas.drawText(String.format("VIPs: %d (%.1f%%)", vipCount, vipPct), cx - 120f, vipLegendY, legendPaint);
    }
}
