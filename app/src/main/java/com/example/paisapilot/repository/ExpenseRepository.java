package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.ExpenseDao;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.utils.Mapper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
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

    public LiveData<List<Expense>> getFilteredExpenses(String query, String dateFilter, List<String> payments, String sortBy) {
        String userId = sessionManager.getUserId();
        StringBuilder sql = new StringBuilder("SELECT * FROM expenses WHERE userId = '").append(userId).append("' AND syncStatus != 'PENDING_DELETE'");

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (title LIKE '%").append(query).append("%' OR category LIKE '%").append(query).append("%' OR note LIKE '%").append(query).append("%')");
        }

        if (dateFilter != null) {
            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            switch (dateFilter) {
                case "Today":
                    sql.append(" AND date >= ").append(cal.getTimeInMillis());
                    break;
                case "Yesterday":
                    long end = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    sql.append(" AND date >= ").append(cal.getTimeInMillis()).append(" AND date < ").append(end);
                    break;
                case "Last 7 Days":
                    cal.add(Calendar.DAY_OF_YEAR, -7);
                    sql.append(" AND date >= ").append(cal.getTimeInMillis());
                    break;
                case "This Month":
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    sql.append(" AND date >= ").append(cal.getTimeInMillis());
                    break;
            }
        }

        if (payments != null && !payments.isEmpty()) {
            sql.append(" AND paymentMethod IN (");
            for (int i = 0; i < payments.size(); i++) {
                sql.append("'").append(payments.get(i)).append("'");
                if (i < payments.size() - 1) sql.append(",");
            }
            sql.append(")");
        }

        if (sortBy != null) {
            switch (sortBy) {
                case "Newest First": sql.append(" ORDER BY date DESC"); break;
                case "Oldest First": sql.append(" ORDER BY date ASC"); break;
                case "Highest Amount": sql.append(" ORDER BY amount DESC"); break;
                case "Lowest Amount": sql.append(" ORDER BY amount ASC"); break;
                case "A-Z": sql.append(" ORDER BY title ASC"); break;
                case "Z-A": sql.append(" ORDER BY title DESC"); break;
            }
        } else {
            sql.append(" ORDER BY date DESC");
        }

        return Transformations.map(expenseDao.getExpensesFiltered(new SimpleSQLiteQuery(sql.toString())), Mapper::toModelList);
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

            expenseDao.insert(Mapper.toEntity(expense, SyncStatus.PENDING_INSERT));
            syncManager.triggerSync();

            budgetRepository.updateBudgetSpent(expense.getCategory(), expense.getAmount(), new BudgetRepository.BudgetCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    callback.onSuccess(true);
                }

                @Override
                public void onError(@NonNull String message) {
                    callback.onSuccess(true);
                }
            });
        });
    }

    public void updateExpense(@NonNull Expense expense, @NonNull ExpenseCallback<Boolean> callback) {
        executor.execute(() -> {
            expenseDao.update(Mapper.toEntity(expense, SyncStatus.PENDING_UPDATE));
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void deleteExpense(@NonNull String expenseId, @NonNull ExpenseCallback<Boolean> callback) {
        executor.execute(() -> {
            // Immediate UI update by marking as pending delete
            // Our DAOs now filter out PENDING_DELETE items from LiveData
            expenseDao.updateSyncStatus(expenseId, SyncStatus.PENDING_DELETE);
            
            // Trigger background sync to Firestore
            syncManager.triggerSync();
            
            // Return success immediately (callback is still useful for UI handling)
            callback.onSuccess(true);
        });
    }

    public void undoDelete(@NonNull String expenseId) {
        executor.execute(() -> {
            // Restore by setting status back to SYNCED (or PENDING_UPDATE if we wanted to be more precise)
            // But since it was already in Firestore, SYNCED is safer to avoid duplicates
            expenseDao.updateSyncStatus(expenseId, SyncStatus.SYNCED);
        });
    }
}
