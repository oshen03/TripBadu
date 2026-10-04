package com.example.tripbadu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * Centralised helper for posting user-facing system notifications (HHDPII requirement).
 *
 * Provides:
 *   • Order-confirmed notification with PendingIntent → HomeActivity (checkout flow).
 *   • Ad-approved notification with PendingIntent → NotificationsActivity (VIP flow).
 *   • General-purpose one-liner for arbitrary alerts.
 *
 * All methods create the required NotificationChannel automatically on Android 8+.
 */
public class NotificationHelper {

    private static final String CHANNEL_ORDERS  = "tripbadu_orders_channel";
    private static final String CHANNEL_ADS     = "tripbadu_ads_channel";

    private static final int NOTIF_ORDER = 3001;
    private static final int NOTIF_AD    = 3002;

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Fires an "Order Placed!" notification after a successful checkout.
     * Tapping it navigates the user back to the marketplace home.
     */
    public static void notifyOrderSuccess(Context context,
                                          String customerName,
                                          double totalAmount) {
        createChannel(context, CHANNEL_ORDERS, "Order Notifications",
                "Confirms when a gear rental order has been placed.");

        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                context, NOTIF_ORDER, intent, pendingIntentFlags());

        String body = String.format("Your rental order for LKR %.2f has been placed. Thank you, %s!",
                totalAmount, customerName);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ORDERS)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("✅ Order Confirmed – TripBadu")
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        dispatch(context, NOTIF_ORDER, builder);
    }

    /**
     * Fires an "Ad Approved!" notification directed to the VIP user.
     * Tapping it opens the NotificationsActivity so they can see all updates.
     */
    public static void notifyAdApproved(Context context, String gearName) {
        createChannel(context, CHANNEL_ADS, "Ad Status Notifications",
                "Notifies when a gear listing ad has been reviewed.");

        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                context, NOTIF_AD, intent, pendingIntentFlags());

        String body = "Your ad for '" + gearName + "' is now live on TripBadu! Tap to view updates.";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ADS)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("🎉 Ad Approved – TripBadu")
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        dispatch(context, NOTIF_AD, builder);
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private static void createChannel(Context context, String channelId,
                                      String name, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private static void dispatch(Context context, int id,
                                 NotificationCompat.Builder builder) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, builder.build());
    }

    private static int pendingIntentFlags() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
    }
}
