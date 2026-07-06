package com.example.paisapilot.model;

import java.util.List;

public class MonthlyReport {
    private String monthName;
    private double totalIncome;
    private double totalExpenses;
    private double totalSavings;
    private double budgetUtilization;
    private String highestCategory;
    private Expense largestExpense;
    private int transactionCount;
    private List<SavingsGoal> goalProgress;
    private List<RecurringExpense> upcomingBills;
    private String aiSummary;
    private List<Expense> allExpenses; // For CSV export

    public MonthlyReport() {}

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }

    public double getTotalSavings() { return totalSavings; }
    public void setTotalSavings(double totalSavings) { this.totalSavings = totalSavings; }

    public double getBudgetUtilization() { return budgetUtilization; }
    public void setBudgetUtilization(double budgetUtilization) { this.budgetUtilization = budgetUtilization; }

    public String getHighestCategory() { return highestCategory; }
    public void setHighestCategory(String highestCategory) { this.highestCategory = highestCategory; }

    public Expense getLargestExpense() { return largestExpense; }
    public void setLargestExpense(Expense largestExpense) { this.largestExpense = largestExpense; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public List<SavingsGoal> getGoalProgress() { return goalProgress; }
    public void setGoalProgress(List<SavingsGoal> goalProgress) { this.goalProgress = goalProgress; }

    public List<RecurringExpense> getUpcomingBills() { return upcomingBills; }
    public void setUpcomingBills(List<RecurringExpense> upcomingBills) { this.upcomingBills = upcomingBills; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public List<Expense> getAllExpenses() { return allExpenses; }
    public void setAllExpenses(List<Expense> allExpenses) { this.allExpenses = allExpenses; }
}
