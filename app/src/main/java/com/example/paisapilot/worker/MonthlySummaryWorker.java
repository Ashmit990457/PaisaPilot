package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.MonthlyArchiveEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MonthlySummaryWorker extends Worker {

    public MonthlySummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_MONTHLY_SUMMARY, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        
        // Check for last month's archive
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        String lastMonthId = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.getTime());
        
        String tag = "monthly_summary_notif_" + lastMonthId;
        if (nsm.wasNotified(tag)) return Result.success();

        MonthlyArchiveEntity archive = db.archiveDao().getArchiveByMonth(userId, lastMonthId);
        if (archive != null) {
            String msg = String.format(Locale.getDefault(), 
                "Last month summary: ₹%.0f spent, ₹%.0f saved. Budget usage: %.0f%%.",
                archive.getTotalExpenses(), archive.getTotalSavings(), archive.getBudgetUsagePercent());
            
            NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_MONTHLY,
                    lastMonthId.hashCode(), "Monthly Summary", msg);
            
            nsm.markNotified(tag);
        }

        return Result.success();
    }
}
