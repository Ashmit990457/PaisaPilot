package com.example.paisapilot.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.model.UserProfile;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SyncWorker extends Worker {

    private final AppDatabase database;
    private final FirebaseFirestore firestore;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.database = AppDatabase.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            syncExpenses();
            syncBudgets();
            syncGoals();
            syncRecurringBills();
            syncUserProfile();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void syncExpenses() throws ExecutionException, InterruptedException {
        List<ExpenseEntity> pending = database.expenseDao().getPendingSyncExpenses();
        for (ExpenseEntity entity : pending) {
            if (entity.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("expenses").document(entity.getExpenseId()).delete());
                database.expenseDao().deleteById(entity.getExpenseId());
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("expenseId", entity.getExpenseId());
                map.put("title", entity.getTitle());
                map.put("category", entity.getCategory());
                map.put("amount", entity.getAmount());
                map.put("date", new Timestamp(entity.getDate()));
                map.put("note", entity.getNote());
                map.put("paymentMethod", entity.getPaymentMethod());
                map.put("userId", entity.getUserId());
                map.put("createdAt", new Timestamp(entity.getCreatedAt()));

                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("expenses").document(entity.getExpenseId()).set(map));
                database.expenseDao().updateSyncStatus(entity.getExpenseId(), SyncStatus.SYNCED);
            }
        }
    }

    private void syncBudgets() throws ExecutionException, InterruptedException {
        List<BudgetEntity> pending = database.budgetDao().getPendingSyncBudgets();
        for (BudgetEntity entity : pending) {
            if (entity.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("budgets").document(entity.getBudgetId()).delete());
                database.budgetDao().deleteById(entity.getBudgetId());
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("budgetId", entity.getBudgetId());
                map.put("category", entity.getCategory());
                map.put("monthlyLimit", entity.getMonthlyLimit());
                map.put("spentAmount", entity.getSpentAmount());
                map.put("remainingAmount", entity.getRemainingAmount());
                map.put("createdAt", new Timestamp(entity.getCreatedAt()));
                map.put("userId", entity.getUserId());

                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("budgets").document(entity.getBudgetId()).set(map));
                database.budgetDao().updateSyncStatus(entity.getBudgetId(), SyncStatus.SYNCED);
            }
        }
    }

    private void syncGoals() throws ExecutionException, InterruptedException {
        List<GoalEntity> pending = database.goalDao().getPendingSyncGoals();
        for (GoalEntity entity : pending) {
            if (entity.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("goals").document(entity.getGoalId()).delete());
                database.goalDao().deleteById(entity.getGoalId());
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("goalId", entity.getGoalId());
                map.put("userId", entity.getUserId());
                map.put("title", entity.getTitle());
                map.put("targetAmount", entity.getTargetAmount());
                map.put("savedAmount", entity.getSavedAmount());
                map.put("remainingAmount", entity.getRemainingAmount());
                map.put("targetDate", new Timestamp(entity.getTargetDate()));
                map.put("createdAt", new Timestamp(entity.getCreatedAt()));
                map.put("completed", entity.isCompleted());

                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("goals").document(entity.getGoalId()).set(map));
                database.goalDao().updateSyncStatus(entity.getGoalId(), SyncStatus.SYNCED);
            }
        }
    }

    private void syncRecurringBills() throws ExecutionException, InterruptedException {
        List<RecurringBillEntity> pending = database.recurringBillDao().getPendingSyncBills();
        for (RecurringBillEntity entity : pending) {
            if (entity.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("recurring").document(entity.getId()).delete());
                database.recurringBillDao().deleteById(entity.getId());
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("id", entity.getId());
                map.put("userId", entity.getUserId());
                map.put("title", entity.getTitle());
                map.put("category", entity.getCategory());
                map.put("amount", entity.getAmount());
                map.put("frequency", entity.getFrequency().name());
                map.put("nextDueDate", new Timestamp(entity.getNextDueDate()));
                if (entity.getLastProcessedDate() != null) {
                    map.put("lastProcessedDate", new Timestamp(entity.getLastProcessedDate()));
                }
                map.put("reminderEnabled", entity.isReminderEnabled());
                map.put("autoAddExpense", entity.isAutoAddExpense());
                map.put("createdAt", new Timestamp(entity.getCreatedAt()));

                Tasks.await(firestore.collection("users").document(entity.getUserId()).collection("recurring").document(entity.getId()).set(map));
                database.recurringBillDao().updateSyncStatus(entity.getId(), SyncStatus.SYNCED);
            }
        }
    }

    private void syncUserProfile() throws ExecutionException, InterruptedException {
        UserProfileEntity entity = database.userProfileDao().getPendingSyncProfile();
        if (entity != null) {
            UserProfile profile = new UserProfile(
                    entity.getFullName(),
                    entity.getOccupation(),
                    entity.getCity(),
                    entity.getMonthlyIncome(),
                    entity.getMonthlySavingGoal(),
                    entity.getCurrency(),
                    entity.getSalaryCreditDate()
            );
            Tasks.await(firestore.collection("users").document(entity.getUserId()).set(profile.toMap()));
            database.userProfileDao().updateSyncStatus(entity.getUserId(), SyncStatus.SYNCED);
        }
    }
}
