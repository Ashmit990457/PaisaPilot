package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.GoalDao;
import com.example.paisapilot.data.local.entity.GoalEntity;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.SavingsGoal;
import com.example.paisapilot.utils.Mapper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavingsRepository {

    private final GoalDao goalDao;
    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SavingsRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.goalDao = db.goalDao();
        this.syncManager = new SyncManager(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface SavingsCallback<T> {
        void onSuccess(@Nullable T data);
        void onError(@NonNull String message);
    }

    public LiveData<List<SavingsGoal>> getAllGoals() {
        String userId = sessionManager.getUserId();
        return Transformations.map(goalDao.getGoalsByUser(userId), Mapper::toGoalModelList);
    }

    public void addGoal(@NonNull SavingsGoal goal, @NonNull SavingsCallback<Boolean> callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        executor.execute(() -> {
            goal.setUserId(userId);
            goal.setCreatedAt(Timestamp.now());
            goal.setRemainingAmount(goal.getTargetAmount() - goal.getSavedAmount());
            goal.setCompleted(goal.getSavedAmount() >= goal.getTargetAmount());
            if (goal.getGoalId() == null) {
                goal.setGoalId(FirebaseFirestore.getInstance().collection("users").document(userId).collection("goals").document().getId());
            }

            goalDao.insert(Mapper.toEntity(goal, SyncStatus.PENDING_INSERT));
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void deleteGoal(@NonNull String goalId, @NonNull SavingsCallback<Boolean> callback) {
        executor.execute(() -> {
            goalDao.updateSyncStatus(goalId, SyncStatus.PENDING_DELETE);
            syncManager.triggerSync();
            callback.onSuccess(true);
        });
    }

    public void addSavings(@NonNull String goalId, double amount, @NonNull SavingsCallback<Boolean> callback) {
        executor.execute(() -> {
            GoalEntity entity = goalDao.getGoalById(goalId);
            if (entity != null) {
                double newSaved = entity.getSavedAmount() + amount;
                entity.setSavedAmount(newSaved);
                entity.setRemainingAmount(Math.max(0, entity.getTargetAmount() - newSaved));
                entity.setCompleted(newSaved >= entity.getTargetAmount());
                entity.setSyncStatus(SyncStatus.PENDING_UPDATE);
                goalDao.update(entity);
                syncManager.triggerSync();
            }
            callback.onSuccess(true);
        });
    }
}
