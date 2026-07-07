package com.example.paisapilot.data.local;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.paisapilot.worker.BudgetMonitorWorker;
import com.example.paisapilot.worker.DailySummaryWorker;
import com.example.paisapilot.worker.ExpenseReminderWorker;
import com.example.paisapilot.worker.GoalMonitorWorker;
import com.example.paisapilot.worker.MonthlySummaryWorker;
import com.example.paisapilot.worker.RecurringReminderWorker;
import com.example.paisapilot.worker.WeeklySummaryWorker;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleAll(Context context) {
        WorkManager wm = WorkManager.getInstance(context);

        // 1. Budget Monitor (Every 4 hours)
        wm.enqueueUniquePeriodicWork("BudgetMonitor", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(BudgetMonitorWorker.class, 4, TimeUnit.HOURS).build());

        // 2. Goal Monitor (Every 12 hours)
        wm.enqueueUniquePeriodicWork("GoalMonitor", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(GoalMonitorWorker.class, 12, TimeUnit.HOURS).build());

        // 3. Recurring Bills Reminder (Every 12 hours)
        wm.enqueueUniquePeriodicWork("RecurringReminder", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(RecurringReminderWorker.class, 12, TimeUnit.HOURS).build());

        // 4. Daily Summary (Once a day)
        wm.enqueueUniquePeriodicWork("DailySummary", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(DailySummaryWorker.class, 24, TimeUnit.HOURS).build());

        // 5. Weekly Summary (Every Sunday - approx every 7 days)
        wm.enqueueUniquePeriodicWork("WeeklySummary", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(WeeklySummaryWorker.class, 7, TimeUnit.DAYS).build());

        // 6. Monthly Summary (Every Month - approx every 30 days)
        wm.enqueueUniquePeriodicWork("MonthlySummary", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(MonthlySummaryWorker.class, 30, TimeUnit.DAYS).build());

        // 7. Expense Reminder (Every 12 hours)
        wm.enqueueUniquePeriodicWork("ExpenseReminder", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(ExpenseReminderWorker.class, 12, TimeUnit.HOURS).build());
    }
}
