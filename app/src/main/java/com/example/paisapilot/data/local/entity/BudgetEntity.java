package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.paisapilot.data.local.SyncStatus;
import java.util.Date;

@Entity(tableName = "budgets")
public class BudgetEntity {
    @PrimaryKey
    @NonNull
    private String budgetId;
    private String category;
    private double monthlyLimit;
    private double spentAmount;
    private double remainingAmount;
    private Date createdAt;
    private String userId;
    private SyncStatus syncStatus;

    public BudgetEntity(@NonNull String budgetId, String category, double monthlyLimit, double spentAmount, double remainingAmount, Date createdAt, String userId, SyncStatus syncStatus) {
        this.budgetId = budgetId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.createdAt = createdAt;
        this.userId = userId;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getBudgetId() { return budgetId; }
    public void setBudgetId(@NonNull String budgetId) { this.budgetId = budgetId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
