package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.BudgetDao;
import com.example.paisapilot.data.local.entity.BudgetEntity;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.Budget;
import com.example.paisapilot.utils.Mapper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BudgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.budgetDao = db.budgetDao();
        this.syncManager = new SyncManager(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface BudgetCallback<T> {
        void onSuccess(@Nullable T data);
        void onError(@NonNull String message);
    }

    public LiveData<List<Budget>> getAllBudgets() {
        String userId = sessionManager.getUserId();
        return Transformations.map(budgetDao.getBudgetsByUser(userId), Mapper::toBudgetModelList);
    }

    public LiveData<List<Budget>> searchBudgets(String query) {
        String userId = sessionManager.getUserId();
        if (query == null || query.trim().isEmpty()) {
            return getAllBudgets();
        }
        return Transformations.map(budgetDao.searchBudgets(userId, query), Mapper::toBudgetModelList);
    }

    public void createBudget(@NonNull Budget budget, @NonNull BudgetCallback<Boolean> callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        executor.execute(() -> {
            budget.setUserId(userId);
            budget.setCreatedAt(Timestamp.now());
            budget.setSpentAmount(0);
            budget.setRemainingAmount(budget.getMonthlyLimit());
            if (budget.getBudgetId() == null) {
                budget.setBudgetId(FirebaseFirestore.getInstance().collection("users").document(userId).collection("budgets").document().getId());
            }

            budgetDao.insert(Mapper.toEntity(budget, SyncStatus.PENDING_INSERT));
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void updateBudget(@NonNull Budget budget, @NonNull BudgetCallback<Boolean> callback) {
        executor.execute(() -> {
            budget.setRemainingAmount(budget.getMonthlyLimit() - budget.getSpentAmount());
            budgetDao.update(Mapper.toEntity(budget, SyncStatus.PENDING_UPDATE));
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void updateBudgetSpent(@NonNull String category, double amountChange, @NonNull BudgetCallback<Boolean> callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        executor.execute(() -> {
            BudgetEntity entity = budgetDao.getBudgetByCategory(userId, category);
            if (entity != null) {
                entity.setSpentAmount(entity.getSpentAmount() + amountChange);
                entity.setRemainingAmount(entity.getRemainingAmount() - amountChange);
                entity.setSyncStatus(SyncStatus.PENDING_UPDATE);
                budgetDao.update(entity);
                syncManager.triggerSync();
            }
            callback.onSuccess(true);
        });
    }

    public void deleteBudget(@NonNull String budgetId, @NonNull BudgetCallback<Boolean> callback) {
        executor.execute(() -> {
            budgetDao.updateSyncStatus(budgetId, SyncStatus.PENDING_DELETE);
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void undoDelete(@NonNull String budgetId) {
        executor.execute(() -> {
            budgetDao.updateSyncStatus(budgetId, SyncStatus.SYNCED);
        });
    }
}
