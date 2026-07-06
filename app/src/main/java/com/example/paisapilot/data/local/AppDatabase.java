package com.example.paisapilot.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.paisapilot.data.local.converters.DataConverters;
import com.example.paisapilot.data.local.dao.*;
import com.example.paisapilot.data.local.entity.*;

@Database(entities = {
        ExpenseEntity.class,
        BudgetEntity.class,
        GoalEntity.class,
        RecurringBillEntity.class,
        UserProfileEntity.class,
        MonthlyArchiveEntity.class
}, version = 2, exportSchema = false)
@TypeConverters({DataConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ExpenseDao expenseDao();
    public abstract BudgetDao budgetDao();
    public abstract GoalDao goalDao();
    public abstract RecurringBillDao recurringBillDao();
    public abstract UserProfileDao userProfileDao();
    public abstract ArchiveDao archiveDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "paisapilot_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
