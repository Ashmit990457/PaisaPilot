package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.ExpenseEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailySummaryWorker extends Worker {

    public DailySummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_DAILY_SUMMARY, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String tag = "daily_summary_" + dateStr;
        if (nsm.wasNotified(tag)) return Result.success();

        List<ExpenseEntity> todayExpenses = db.expenseDao().getExpensesByUserAfterSync(userId, startOfDay);
        if (todayExpenses.isEmpty()) return Result.success();

        double total = 0;
        StringBuilder categories = new StringBuilder();
        java.util.Map<String, Double> catMap = new java.util.HashMap<>();
        
        for (ExpenseEntity e : todayExpenses) {
            total += e.getAmount();
            catMap.put(e.getCategory(), catMap.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        for (java.util.Map.Entry<String, Double> entry : catMap.entrySet()) {
            categories.append(entry.getKey()).append(" ₹").append(String.format(Locale.getDefault(), "%.0f", entry.getValue())).append(", ");
        }
        
        if (categories.length() > 2) categories.setLength(categories.length() - 2);

        NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_DAILY, 
                dateStr.hashCode(), "Today's Summary", 
                "₹" + String.format(Locale.getDefault(), "%.0f", total) + " spent today. " + categories.toString());
        
        nsm.markNotified(tag);

        return Result.success();
    }
}
