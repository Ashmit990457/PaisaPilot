package com.example.paisapilot.model;

public class Forecast {
    public enum RiskLevel {
        SAFE, MODERATE, HIGH
    }

    private double averageDailySpending;
    private double projectedExpense;
    private double forecastedSavings;
    private RiskLevel riskLevel;
    private String forecastMessage;

    public Forecast() {
    }

    public Forecast(double averageDailySpending, double projectedExpense, double forecastedSavings, RiskLevel riskLevel, String forecastMessage) {
        this.averageDailySpending = averageDailySpending;
        this.projectedExpense = projectedExpense;
        this.forecastedSavings = forecastedSavings;
        this.riskLevel = riskLevel;
        this.forecastMessage = forecastMessage;
    }

    public double getAverageDailySpending() {
        return averageDailySpending;
    }

    public void setAverageDailySpending(double averageDailySpending) {
        this.averageDailySpending = averageDailySpending;
    }

    public double getProjectedExpense() {
        return projectedExpense;
    }

    public void setProjectedExpense(double projectedExpense) {
        this.projectedExpense = projectedExpense;
    }

    public double getForecastedSavings() {
        return forecastedSavings;
    }

    public void setForecastedSavings(double forecastedSavings) {
        this.forecastedSavings = forecastedSavings;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getForecastMessage() {
        return forecastMessage;
    }

    public void setForecastMessage(String forecastMessage) {
        this.forecastMessage = forecastMessage;
    }
}
