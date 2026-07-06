package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.model.RecurringExpense;
import java.util.Date;

@Entity(tableName = "recurring_bills")
public class RecurringBillEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String userId;
    private String title;
    private String category;
    private double amount;
    private RecurringExpense.Frequency frequency;
    private Date nextDueDate;
    private Date lastProcessedDate;
    private boolean reminderEnabled;
    private boolean autoAddExpense;
    private Date createdAt;
    private SyncStatus syncStatus;

    public RecurringBillEntity(@NonNull String id, String userId, String title, String category, double amount, RecurringExpense.Frequency frequency, Date nextDueDate, Date lastProcessedDate, boolean reminderEnabled, boolean autoAddExpense, Date createdAt, SyncStatus syncStatus) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.lastProcessedDate = lastProcessedDate;
        this.reminderEnabled = reminderEnabled;
        this.autoAddExpense = autoAddExpense;
        this.createdAt = createdAt;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public RecurringExpense.Frequency getFrequency() { return frequency; }
    public void setFrequency(RecurringExpense.Frequency frequency) { this.frequency = frequency; }

    public Date getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(Date nextDueDate) { this.nextDueDate = nextDueDate; }

    public Date getLastProcessedDate() { return lastProcessedDate; }
    public void setLastProcessedDate(Date lastProcessedDate) { this.lastProcessedDate = lastProcessedDate; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public boolean isAutoAddExpense() { return autoAddExpense; }
    public void setAutoAddExpense(boolean autoAddExpense) { this.autoAddExpense = autoAddExpense; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
