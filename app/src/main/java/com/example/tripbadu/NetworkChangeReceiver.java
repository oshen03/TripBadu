package com.example.tripbadu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * NetworkChangeReceiver – BroadcastReceiver implementation (HHDPII syllabus requirement).
 *
 * Monitors real-time device connectivity changes. Fires a system notification
 * with a PendingIntent when the network goes offline, and again when it
 * reconnects. Registered dynamically in HomeActivity and also declared in the
 * manifest so the system can wake it on boot or background connectivity events.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID   = "tripbadu_network_channel";
    private static final int    NOTIF_ONLINE  = 2001;
    private static final int    NOTIF_OFFLINE = 2002;

    /** Last known connectivity state – prevents duplicate notifications. */
    private static Boolean lastState = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOnline = isNetworkAvailable(context);

        // Suppress duplicate firings (Android may call onReceive multiple times)
        if (lastState != null && lastState == isOnline) return;
        lastState = isOnline;

        ensureChannelExists(context);

        if (isOnline) {
            showNotification(
                    context,
                    NOTIF_ONLINE,
                    "TripBadu – Back Online",
                    "Your internet connection has been restored.",
                    HomeActivity.class
            );
        } else {
            showNotification(
                    context,
                    NOTIF_OFFLINE,
                    "TripBadu – No Internet",
                    "You are offline. Some features may be unavailable.",
                    HomeActivity.class
            );
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Returns true when any active network has internet capability. */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /** Creates a notification channel (Android 8+). */
    private void ensureChannelExists(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Network Status",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifies when internet connectivity changes.");
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /**
     * Posts a dismissible status notification. Tapping it opens the supplied
     * Activity via a {@link PendingIntent}.
     */
    private void showNotification(Context context, int notifId,
                                  String title, String body,
                                  Class<?> targetActivity) {
        Intent tapIntent = new Intent(context, targetActivity);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, notifId, tapIntent, flags);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notifId, builder.build());
    }
}
