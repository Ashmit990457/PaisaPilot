package com.example.paisapilot.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.paisapilot.ui.activities.MainActivity;

public class NotificationHelper {
    public static final String CHANNEL_GENERAL = "general";
    public static final String CHANNEL_BUDGET = "budget_alerts";
    public static final String CHANNEL_GOALS = "savings_goals";
    public static final String CHANNEL_RECURRING = "recurring_bills";
    public static final String CHANNEL_DAILY = "daily_summary";
    public static final String CHANNEL_WEEKLY = "weekly_summary";
    public static final String CHANNEL_MONTHLY = "monthly_summary";
    public static final String CHANNEL_AI = "ai_insights";

    public static void showNotification(Context context, String channelId, int id, String title, String content) {
        createAllChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(id, builder.build());
        }
    }
    
    // For backward compatibility with existing code
    public static void showBillReminder(Context context, String title, String content) {
        showNotification(context, CHANNEL_RECURRING, (int) System.currentTimeMillis(), title, content);
    }

    public static void showNotification(Context context, String title, String message) {
        showNotification(context, CHANNEL_GENERAL, (int) System.currentTimeMillis(), title, message);
    }

    public static void createAllChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_BUDGET, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_GOALS, "Savings Goals", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_RECURRING, "Recurring Bills", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_DAILY, "Daily Summary", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_WEEKLY, "Weekly Summary", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_MONTHLY, "Monthly Summary", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_AI, "AI Insights", NotificationManager.IMPORTANCE_DEFAULT));
        }
    }
}
