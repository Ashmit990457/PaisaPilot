package com.example.paisapilot.data.local;

import android.content.Context;
import android.util.Log;

import com.example.paisapilot.data.local.dao.*;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LifecycleManager {
    private static final String TAG = "LifecycleManager";
    private final Context context;
    private final AppDatabase database;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LifecycleManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void checkAndRunRollover() {
        if (!sessionManager.isLoggedIn()) return;

        String currentMonthId = getCurrentMonthId();
        String lastMonthId = sessionManager.getLastProcessedMonth();

        if (lastMonthId.isEmpty()) {
            // First time tracking, just set current
            sessionManager.setLastProcessedMonth(currentMonthId);
            return;
        }

        if (!currentMonthId.equals(lastMonthId)) {
            Log.d(TAG, "New month detected: " + currentMonthId + ". Running rollover...");
            runMonthlyRollover(lastMonthId, currentMonthId);
        }
    }

    private String getCurrentMonthId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void runMonthlyRollover(String oldMonthId, String newMonthId) {
        executor.execute(() -> {
            try {
                String userId = sessionManager.getUserId();
                if (userId == null) return;

                // 1. Snapshot previous month
                archiveMonth(userId, oldMonthId);

                // 2. Reset Budgets
                resetBudgets(userId);

                // 3. Notify User
                String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
                NotificationHelper.showNotification(context, "Monthly Reset", 
                        "Welcome to " + monthName + "! Your budgets have been refreshed.");

                // 4. Update session
                sessionManager.setLastProcessedMonth(newMonthId);
                Log.d(TAG, "Monthly rollover completed successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error during monthly rollover", e);
            }
        });
    }

    private void archiveMonth(String userId, String monthId) {
        try {
            // 1. Calculate time range for the monthId (yyyy-MM)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date monthDate = sdf.parse(monthId);
            if (monthDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(monthDate);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            long start = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, 1);
            long end = cal.getTimeInMillis();

            // 2. Query real data from Room
            List<ExpenseEntity> expenses = database.expenseDao().getExpensesByUserAfterSync(userId, start);
            double totalExp = 0;
            for (ExpenseEntity e : expenses) {
                if (e.getDate().getTime() < end) {
                    totalExp += e.getAmount();
                }
            }

            List<BudgetEntity> budgets = database.budgetDao().getBudgetsByUserSync(userId);
            double totalBudgetLimit = 0;
            double totalSpentInBudgets = 0;
            for (BudgetEntity b : budgets) {
                totalBudgetLimit += b.getMonthlyLimit();
                totalSpentInBudgets += b.getSpentAmount();
            }
            double budgetUsage = totalBudgetLimit > 0 ? (totalSpentInBudgets / totalBudgetLimit) * 100 : 0;

            UserProfileEntity profile = database.userProfileDao().getUserProfileSync(userId);
            double income = profile != null ? profile.getMonthlyIncome() : 0;

            // 3. Save Archive
            MonthlyArchiveEntity archive = new MonthlyArchiveEntity(
                    monthId, userId, income, totalExp, 0, budgetUsage, 0, 0,
                    "Automated monthly archive for " + monthId, System.currentTimeMillis()
            );
            database.archiveDao().insert(archive);
            Log.d(TAG, "Archived month: " + monthId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to archive month: " + monthId, e);
        }
    }

    private void resetBudgets(String userId) {
        List<BudgetEntity> budgets = database.budgetDao().getBudgetsByUserSync(userId);
        for (BudgetEntity budget : budgets) {
            budget.setSpentAmount(0);
            budget.setRemainingAmount(budget.getMonthlyLimit());
            budget.setSyncStatus(SyncStatus.PENDING_UPDATE);
            database.budgetDao().update(budget);
        }
    }
}
