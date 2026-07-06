package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.ExpenseEntity;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByUser(String userId);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByUserAfter(String userId, long startDate);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate ORDER BY date DESC")
    List<ExpenseEntity> getExpensesByUserAfterSync(String userId, long startDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExpenseEntity expense);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExpenseEntity> expenses);

    @Update
    void update(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    void deleteById(String expenseId);

    @Query("SELECT * FROM expenses WHERE syncStatus != 'SYNCED'")
    List<ExpenseEntity> getPendingSyncExpenses();

    @Query("UPDATE expenses SET syncStatus = :status WHERE expenseId = :expenseId")
    void updateSyncStatus(String expenseId, SyncStatus status);

    @Query("DELETE FROM expenses")
    void deleteAll();
}
