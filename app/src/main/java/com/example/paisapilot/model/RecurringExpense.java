package com.example.paisapilot.model;

import com.google.firebase.Timestamp;

public class RecurringExpense {
    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    private String id;
    private String userId;
    private String title;
    private String category;
    private double amount;
    private Frequency frequency;
    private Timestamp nextDueDate;
    private Timestamp lastProcessedDate;
    private boolean reminderEnabled;
    private boolean autoAddExpense;
    private Timestamp createdAt;

    public RecurringExpense() {
        // Required for Firestore
    }

    public RecurringExpense(String id, String userId, String title, String category, double amount, Frequency frequency, Timestamp nextDueDate, boolean reminderEnabled, boolean autoAddExpense) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.reminderEnabled = reminderEnabled;
        this.autoAddExpense = autoAddExpense;
        this.createdAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public Timestamp getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(Timestamp nextDueDate) { this.nextDueDate = nextDueDate; }

    public Timestamp getLastProcessedDate() { return lastProcessedDate; }
    public void setLastProcessedDate(Timestamp lastProcessedDate) { this.lastProcessedDate = lastProcessedDate; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public boolean isAutoAddExpense() { return autoAddExpense; }
    public void setAutoAddExpense(boolean autoAddExpense) { this.autoAddExpense = autoAddExpense; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
