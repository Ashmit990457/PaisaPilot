package com.example.paisapilot.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.ExpenseDao;
import com.example.paisapilot.data.local.dao.RecurringBillDao;
import com.example.paisapilot.data.local.entity.ExpenseEntity;
import com.example.paisapilot.data.local.entity.RecurringBillEntity;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.utils.NotificationHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RecurringExpenseWorker extends Worker {

    private final RecurringBillDao recurringDao;
    private final ExpenseDao expenseDao;
    private final SyncManager syncManager;

    public RecurringExpenseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        AppDatabase db = AppDatabase.getInstance(context);
        this.recurringDao = db.recurringBillDao();
        this.expenseDao = db.expenseDao();
        this.syncManager = new SyncManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("RecurringWorker", "Starting background processing of recurring expenses");
        
        try {
            Date now = new Date();
            List<RecurringBillEntity> dueBills = recurringDao.getDueRecurringBills(now.getTime());
            
            for (RecurringBillEntity bill : dueBills) {
                if (bill.isAutoAddExpense()) {
                    ExpenseEntity expense = new ExpenseEntity(
                            UUID.randomUUID().toString(),
                            bill.getTitle(),
                            bill.getCategory(),
                            bill.getAmount(),
                            bill.getNextDueDate(),
                            "Auto-added recurring expense",
                            "Other",
                            bill.getUserId(),
                            new Date(),
                            SyncStatus.PENDING_INSERT
                    );
                    expenseDao.insert(expense);
                    
                    if (bill.isReminderEnabled()) {
                        NotificationHelper.showBillReminder(getApplicationContext(), "Bill Processed", bill.getTitle() + " has been auto-added.");
                    }
                } else if (bill.isReminderEnabled()) {
                    NotificationHelper.showBillReminder(getApplicationContext(), "Bill Reminder", bill.getTitle() + " is due.");
                }

                // Advance nextDueDate
                bill.setLastProcessedDate(now);
                bill.setNextDueDate(calculateNextDate(bill.getNextDueDate(), bill.getFrequency()));
                bill.setSyncStatus(SyncStatus.PENDING_UPDATE);
                recurringDao.update(bill);
            }
            
            if (!dueBills.isEmpty()) {
                syncManager.triggerSync();
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("RecurringWorker", "Error processing recurring expenses", e);
            return Result.retry();
        }
    }

    private Date calculateNextDate(Date current, RecurringExpense.Frequency frequency) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        switch (frequency) {
            case DAILY: cal.add(Calendar.DAY_OF_YEAR, 1); break;
            case WEEKLY: cal.add(Calendar.WEEK_OF_YEAR, 1); break;
            case MONTHLY: cal.add(Calendar.MONTH, 1); break;
            case YEARLY: cal.add(Calendar.YEAR, 1); break;
        }
        return cal.getTime();
    }
}
