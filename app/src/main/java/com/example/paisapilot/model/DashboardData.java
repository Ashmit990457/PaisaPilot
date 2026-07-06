package com.example.paisapilot.model;

import java.util.Map;

public class DashboardData {
    private double totalExpense;
    private double totalBudget;
    private double remainingBudget;
    private int expenseCount;
    private String highestCategory;
    private Map<String, Double> categoryWiseExpenses;
    private Map<Integer, Double> weeklyExpenses; // Week number to amount
    private Forecast forecast;

    public DashboardData() {
    }

    public DashboardData(double totalExpense, double totalBudget, double remainingBudget, int expenseCount, String highestCategory, Map<String, Double> categoryWiseExpenses, Map<Integer, Double> weeklyExpenses, Forecast forecast) {
        this.totalExpense = totalExpense;
        this.totalBudget = totalBudget;
        this.remainingBudget = remainingBudget;
        this.expenseCount = expenseCount;
        this.highestCategory = highestCategory;
        this.categoryWiseExpenses = categoryWiseExpenses;
        this.weeklyExpenses = weeklyExpenses;
        this.forecast = forecast;
    }

    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }

    public String getHighestCategory() {
        return highestCategory;
    }

    public void setHighestCategory(String highestCategory) {
        this.highestCategory = highestCategory;
    }

    public Map<String, Double> getCategoryWiseExpenses() {
        return categoryWiseExpenses;
    }

    public void setCategoryWiseExpenses(Map<String, Double> categoryWiseExpenses) {
        this.categoryWiseExpenses = categoryWiseExpenses;
    }

    public Map<Integer, Double> getWeeklyExpenses() {
        return weeklyExpenses;
    }

    public void setWeeklyExpenses(Map<Integer, Double> weeklyExpenses) {
        this.weeklyExpenses = weeklyExpenses;
    }
}
