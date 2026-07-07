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

public class ExpenseReminderWorker extends Worker {

    public ExpenseReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_EXPENSE_REMINDER, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        List<ExpenseEntity> todayExpenses = db.expenseDao().getExpensesByUserAfterSync(userId, startOfDay);
        
        if (todayExpenses.isEmpty()) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String tag = "expense_reminder_" + dateStr;
            
            // Only remind once in the afternoon/evening
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour >= 18 && !nsm.wasNotified(tag)) {
                NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_GENERAL,
                        tag.hashCode(), "Expense Reminder", "Don't forget to record today's expenses!");
                nsm.markNotified(tag);
            }
        }

        return Result.success();
    }
}
