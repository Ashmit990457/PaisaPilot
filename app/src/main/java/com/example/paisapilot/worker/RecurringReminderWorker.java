package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.RecurringBillEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringReminderWorker extends Worker {

    public RecurringReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_RECURRING_REMINDER, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        List<RecurringBillEntity> bills = db.recurringBillDao().getRecurringBillsByUserSync(userId);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (RecurringBillEntity bill : bills) {
            Calendar due = Calendar.getInstance();
            due.setTime(bill.getNextDueDate());
            due.set(Calendar.HOUR_OF_DAY, 0);
            due.set(Calendar.MINUTE, 0);
            due.set(Calendar.SECOND, 0);
            due.set(Calendar.MILLISECOND, 0);

            String tagDate = sdf.format(due.getTime());
            String tag = "recurring_" + bill.getId() + "_" + tagDate;

            if (due.equals(today)) {
                if (!nsm.wasNotified(tag + "_today")) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_RECURRING,
                            bill.getId().hashCode(), "Bill Due Today",
                            bill.getTitle() + " (₹" + bill.getAmount() + ") is due today.");
                    nsm.markNotified(tag + "_today");
                }
            } else if (due.equals(tomorrow)) {
                if (!nsm.wasNotified(tag + "_tomorrow")) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_RECURRING,
                            bill.getId().hashCode(), "Bill Due Tomorrow",
                            bill.getTitle() + " (₹" + bill.getAmount() + ") is due tomorrow.");
                    nsm.markNotified(tag + "_tomorrow");
                }
            }
        }

        return Result.success();
    }
}
