package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.paisapilot.data.local.SyncStatus;
import java.util.Date;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey
    @NonNull
    private String expenseId;
    private String title;
    private String category;
    private double amount;
    private Date date;
    private String note;
    private String paymentMethod;
    private String userId;
    private Date createdAt;
    private SyncStatus syncStatus;

    public ExpenseEntity(@NonNull String expenseId, String title, String category, double amount, Date date, String note, String paymentMethod, String userId, Date createdAt, SyncStatus syncStatus) {
        this.expenseId = expenseId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
        this.createdAt = createdAt;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getExpenseId() { return expenseId; }
    public void setExpenseId(@NonNull String expenseId) { this.expenseId = expenseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
