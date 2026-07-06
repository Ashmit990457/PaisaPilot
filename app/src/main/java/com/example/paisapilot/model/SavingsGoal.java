package com.example.paisapilot.model;

import com.google.firebase.Timestamp;

public class SavingsGoal {
    private String goalId;
    private String userId;
    private String title;
    private double targetAmount;
    private double savedAmount;
    private double remainingAmount;
    private Timestamp targetDate;
    private Timestamp createdAt;
    private boolean completed;

    public SavingsGoal() {
        // Required for Firestore
    }

    public SavingsGoal(String goalId, String userId, String title, double targetAmount, double savedAmount, Timestamp targetDate, Timestamp createdAt, boolean completed) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.remainingAmount = targetAmount - savedAmount;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
        this.completed = completed;
    }

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
        updateRemaining();
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
        updateRemaining();
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Timestamp getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Timestamp targetDate) {
        this.targetDate = targetDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private void updateRemaining() {
        this.remainingAmount = Math.max(0, targetAmount - savedAmount);
        if (this.savedAmount >= this.targetAmount) {
            this.completed = true;
        }
    }
    
    public int getPercentage() {
        if (targetAmount <= 0) return 0;
        return (int) Math.min(100, (savedAmount / targetAmount) * 100);
    }
}
