package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.ExpenseEntity;
import com.example.paisapilot.data.local.entity.UserProfileEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklySummaryWorker extends Worker {

    public WeeklySummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_WEEKLY_SUMMARY, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startOfWeek = cal.getTimeInMillis();

        String dateStr = new SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(new Date());
        String tag = "weekly_summary_" + dateStr;
        if (nsm.wasNotified(tag)) return Result.success();

        List<ExpenseEntity> weekExpenses = db.expenseDao().getExpensesByUserAfterSync(userId, startOfWeek);
        if (weekExpenses.isEmpty()) return Result.success();

        double total = 0;
        String highestCategory = "None";
        java.util.Map<String, Double> catMap = new java.util.HashMap<>();
        
        for (ExpenseEntity e : weekExpenses) {
            total += e.getAmount();
            catMap.put(e.getCategory(), catMap.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        double max = -1;
        for (java.util.Map.Entry<String, Double> entry : catMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                highestCategory = entry.getKey();
            }
        }

        NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_WEEKLY, 
                dateStr.hashCode(), "Weekly Summary", 
                "You spent ₹" + String.format(Locale.getDefault(), "%.0f", total) + " this week. Highest spending was on " + highestCategory + ".");
        
        nsm.markNotified(tag);

        return Result.success();
    }
}
