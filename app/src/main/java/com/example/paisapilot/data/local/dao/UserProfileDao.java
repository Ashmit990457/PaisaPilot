package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.UserProfileEntity;

@Dao
public interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    LiveData<UserProfileEntity> getUserProfile(String userId);

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    UserProfileEntity getUserProfileSync(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfileEntity profile);

    @Update
    void update(UserProfileEntity profile);

    @Query("SELECT * FROM user_profile WHERE syncStatus != 'SYNCED' LIMIT 1")
    UserProfileEntity getPendingSyncProfile();

    @Query("UPDATE user_profile SET syncStatus = :status WHERE userId = :userId")
    void updateSyncStatus(String userId, SyncStatus status);

    @Query("DELETE FROM user_profile")
    void deleteAll();
}
