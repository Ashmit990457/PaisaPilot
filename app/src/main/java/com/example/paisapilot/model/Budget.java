package com.example.paisapilot.model;

import com.google.firebase.Timestamp;

/**
 * Budget model for Firestore documents.
 */
public class Budget {
    private String budgetId;
    private String category;
    private double monthlyLimit;
    private double spentAmount;
    private double remainingAmount;
    private Timestamp createdAt;
    private String userId;

    public Budget() {
        // Required empty constructor for Firestore
    }

    public Budget(String budgetId, String category, double monthlyLimit, double spentAmount, double remainingAmount, Timestamp createdAt, String userId) {
        this.budgetId = budgetId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
