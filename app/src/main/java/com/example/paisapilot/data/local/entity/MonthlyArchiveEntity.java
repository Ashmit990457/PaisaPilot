package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "monthly_archives")
public class MonthlyArchiveEntity {
    @PrimaryKey
    @NonNull
    private String monthId; // e.g., "2026-07"
    private String userId;
    private double totalIncome;
    private double totalExpenses;
    private double totalSavings;
    private double budgetUsagePercent;
    private int goalsProgressPercent;
    private int recurringBillsPaid;
    private String aiInsightSummary;
    private long archivedAt;

    public MonthlyArchiveEntity(@NonNull String monthId, String userId, double totalIncome, 
                               double totalExpenses, double totalSavings, double budgetUsagePercent, 
                               int goalsProgressPercent, int recurringBillsPaid, 
                               String aiInsightSummary, long archivedAt) {
        this.monthId = monthId;
        this.userId = userId;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.totalSavings = totalSavings;
        this.budgetUsagePercent = budgetUsagePercent;
        this.goalsProgressPercent = goalsProgressPercent;
        this.recurringBillsPaid = recurringBillsPaid;
        this.aiInsightSummary = aiInsightSummary;
        this.archivedAt = archivedAt;
    }

    @NonNull
    public String getMonthId() { return monthId; }
    public void setMonthId(@NonNull String monthId) { this.monthId = monthId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }
    public double getTotalSavings() { return totalSavings; }
    public void setTotalSavings(double totalSavings) { this.totalSavings = totalSavings; }
    public double getBudgetUsagePercent() { return budgetUsagePercent; }
    public void setBudgetUsagePercent(double budgetUsagePercent) { this.budgetUsagePercent = budgetUsagePercent; }
    public int getGoalsProgressPercent() { return goalsProgressPercent; }
    public void setGoalsProgressPercent(int goalsProgressPercent) { this.goalsProgressPercent = goalsProgressPercent; }
    public int getRecurringBillsPaid() { return recurringBillsPaid; }
    public void setRecurringBillsPaid(int recurringBillsPaid) { this.recurringBillsPaid = recurringBillsPaid; }
    public String getAiInsightSummary() { return aiInsightSummary; }
    public void setAiInsightSummary(String aiInsightSummary) { this.aiInsightSummary = aiInsightSummary; }
    public long getArchivedAt() { return archivedAt; }
    public void setArchivedAt(long archivedAt) { this.archivedAt = archivedAt; }
}
