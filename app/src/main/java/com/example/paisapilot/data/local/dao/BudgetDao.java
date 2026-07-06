package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.BudgetEntity;
import java.util.List;

@Dao
public interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    LiveData<List<BudgetEntity>> getBudgetsByUser(String userId);

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    List<BudgetEntity> getBudgetsByUserSync(String userId);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category LIMIT 1")
    BudgetEntity getBudgetByCategory(String userId, String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BudgetEntity budget);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BudgetEntity> budgets);

    @Update
    void update(BudgetEntity budget);

    @Query("DELETE FROM budgets WHERE budgetId = :budgetId")
    void deleteById(String budgetId);

    @Query("SELECT * FROM budgets WHERE syncStatus != 'SYNCED'")
    List<BudgetEntity> getPendingSyncBudgets();

    @Query("UPDATE budgets SET syncStatus = :status WHERE budgetId = :budgetId")
    void updateSyncStatus(String budgetId, SyncStatus status);

    @Query("DELETE FROM budgets")
    void deleteAll();
}
