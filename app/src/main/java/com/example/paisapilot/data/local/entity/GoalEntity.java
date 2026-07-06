package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.paisapilot.data.local.SyncStatus;
import java.util.Date;

@Entity(tableName = "goals")
public class GoalEntity {
    @PrimaryKey
    @NonNull
    private String goalId;
    private String userId;
    private String title;
    private double targetAmount;
    private double savedAmount;
    private double remainingAmount;
    private Date targetDate;
    private Date createdAt;
    private boolean completed;
    private SyncStatus syncStatus;

    public GoalEntity(@NonNull String goalId, String userId, String title, double targetAmount, double savedAmount, double remainingAmount, Date targetDate, Date createdAt, boolean completed, SyncStatus syncStatus) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.remainingAmount = remainingAmount;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
        this.completed = completed;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getGoalId() { return goalId; }
    public void setGoalId(@NonNull String goalId) { this.goalId = goalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
