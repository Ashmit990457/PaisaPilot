package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.dao.UserProfileDao;
import com.example.paisapilot.data.remote.SyncManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.UserProfile;
import com.example.paisapilot.utils.Mapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileRepository {

    private final UserProfileDao profileDao;
    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserProfileRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.profileDao = db.userProfileDao();
        this.syncManager = new SyncManager(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface ProfileCallback {
        void onSuccess();
        void onFailure(@NonNull String message);
    }

    public LiveData<UserProfile> getUserProfile() {
        String userId = sessionManager.getUserId();
        return Transformations.map(profileDao.getUserProfile(userId), entity -> 
                entity == null ? null : Mapper.toModel(entity));
    }

    public void saveUserProfile(@NonNull UserProfile profile, @NonNull final ProfileCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        executor.execute(() -> {
            profileDao.insert(Mapper.toEntity(profile, userId, SyncStatus.PENDING_INSERT));
            syncManager.triggerSync();
            callback.onSuccess();
        });
    }
}
