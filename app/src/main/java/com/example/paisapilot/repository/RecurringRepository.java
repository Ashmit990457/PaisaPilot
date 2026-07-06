package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.RecurringBillDao;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.utils.Mapper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecurringRepository {

    private final RecurringBillDao recurringBillDao;
    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RecurringRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.recurringBillDao = db.recurringBillDao();
        this.syncManager = new SyncManager(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface RecurringCallback<T> {
        void onSuccess(@Nullable T data);
        void onError(@NonNull String message);
    }

    public LiveData<List<RecurringExpense>> getAllRecurring() {
        String userId = sessionManager.getUserId();
        return Transformations.map(recurringBillDao.getRecurringBillsByUser(userId), Mapper::toRecurringModelList);
    }

    public void createRecurring(@NonNull RecurringExpense recurring, @NonNull RecurringCallback<Boolean> callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        executor.execute(() -> {
            recurring.setUserId(userId);
            if (recurring.getId() == null) {
                recurring.setId(FirebaseFirestore.getInstance().collection("users").document(userId).collection("recurring").document().getId());
            }

            recurringBillDao.insert(Mapper.toEntity(recurring, SyncStatus.PENDING_INSERT));
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void deleteRecurring(@NonNull String id, @NonNull RecurringCallback<Boolean> callback) {
        executor.execute(() -> {
            recurringBillDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE);
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }
}
