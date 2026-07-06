package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.RecurringBillEntity;
import java.util.List;

@Dao
public interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills WHERE userId = :userId")
    LiveData<List<RecurringBillEntity>> getRecurringBillsByUser(String userId);

    @Query("SELECT * FROM recurring_bills WHERE userId = :userId")
    List<RecurringBillEntity> getRecurringBillsByUserSync(String userId);

    @Query("SELECT * FROM recurring_bills WHERE nextDueDate <= :now")
    List<RecurringBillEntity> getDueRecurringBills(long now);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecurringBillEntity bill);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecurringBillEntity> bills);

    @Update
    void update(RecurringBillEntity bill);

    @Query("DELETE FROM recurring_bills WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM recurring_bills WHERE syncStatus != 'SYNCED'")
    List<RecurringBillEntity> getPendingSyncBills();

    @Query("UPDATE recurring_bills SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(String id, SyncStatus status);

    @Query("DELETE FROM recurring_bills")
    void deleteAll();
}
