package com.example.paisapilot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.paisapilot.data.local.entity.MonthlyArchiveEntity;
import java.util.List;

@Dao
public interface ArchiveDao {
    @Query("SELECT * FROM monthly_archives WHERE userId = :userId ORDER BY archivedAt DESC")
    LiveData<List<MonthlyArchiveEntity>> getArchivesByUser(String userId);

    @Query("SELECT * FROM monthly_archives WHERE monthId = :monthId AND userId = :userId LIMIT 1")
    MonthlyArchiveEntity getArchiveByMonth(String userId, String monthId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyArchiveEntity archive);
}
