package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.ExpenseDao;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.utils.Mapper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final BudgetRepository budgetRepository;

    public ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.syncManager = new SyncManager(context);
        this.sessionManager = SessionManager.getInstance(context);
        this.budgetRepository = new BudgetRepository(context);
    }

    public interface ExpenseCallback<T> {
        void onSuccess(@Nullable T data);
        void onError(@NonNull String message);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        String userId = sessionManager.getUserId();
        return Transformations.map(expenseDao.getExpensesByUser(userId), Mapper::toModelList);
    }

    public void addExpense(@NonNull Expense expense, @NonNull ExpenseCallback<Boolean> callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        executor.execute(() -> {
            expense.setUserId(userId);
            expense.setCreatedAt(com.google.firebase.Timestamp.now());
            if (expense.getExpenseId() == null) {
                expense.setExpenseId(FirebaseFirestore.getInstance().collection("users").document(userId).collection("expenses").document().getId());
            }

            // Save to Room immediately
            expenseDao.insert(Mapper.toEntity(expense, SyncStatus.PENDING_INSERT));
            
            // Trigger sync
            syncManager.triggerSync();

            // Update budget (in background)
            budgetRepository.updateBudgetSpent(expense.getCategory(), expense.getAmount(), new BudgetRepository.BudgetCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    callback.onSuccess(true);
                }

                @Override
                public void onError(@NonNull String message) {
                    callback.onSuccess(true); // Still added successfully
                }
            });
        });
    }

    public void deleteExpense(@NonNull String expenseId, @NonNull ExpenseCallback<Boolean> callback) {
        executor.execute(() -> {
            expenseDao.updateSyncStatus(expenseId, SyncStatus.PENDING_DELETE);
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }
}
