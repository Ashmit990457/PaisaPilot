package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.GoalEntity;
import java.util.List;

@Dao
public interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId")
    LiveData<List<GoalEntity>> getGoalsByUser(String userId);

    @Query("SELECT * FROM goals WHERE userId = :userId")
    List<GoalEntity> getGoalsByUserSync(String userId);

    @Query("SELECT * FROM goals WHERE goalId = :goalId LIMIT 1")
    GoalEntity getGoalById(String goalId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GoalEntity goal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoalEntity> goals);

    @Update
    void update(GoalEntity goal);

    @Query("DELETE FROM goals WHERE goalId = :goalId")
    void deleteById(String goalId);

    @Query("SELECT * FROM goals WHERE syncStatus != 'SYNCED'")
    List<GoalEntity> getPendingSyncGoals();

    @Query("UPDATE goals SET syncStatus = :status WHERE goalId = :goalId")
    void updateSyncStatus(String goalId, SyncStatus status);

    @Query("DELETE FROM goals")
    void deleteAll();
}
