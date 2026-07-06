package com.example.paisapilot.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.paisapilot.data.local.SyncStatus;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @PrimaryKey
    @NonNull
    private String userId;
    private String fullName;
    private String occupation;
    private String city;
    private double monthlyIncome;
    private double monthlySavingGoal;
    private String currency;
    private int salaryCreditDate;
    private SyncStatus syncStatus;

    public UserProfileEntity(@NonNull String userId, String fullName, String occupation, String city, double monthlyIncome, double monthlySavingGoal, String currency, int salaryCreditDate, SyncStatus syncStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.occupation = occupation;
        this.city = city;
        this.monthlyIncome = monthlyIncome;
        this.monthlySavingGoal = monthlySavingGoal;
        this.currency = currency;
        this.salaryCreditDate = salaryCreditDate;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public double getMonthlySavingGoal() { return monthlySavingGoal; }
    public void setMonthlySavingGoal(double monthlySavingGoal) { this.monthlySavingGoal = monthlySavingGoal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getSalaryCreditDate() { return salaryCreditDate; }
    public void setSalaryCreditDate(int salaryCreditDate) { this.salaryCreditDate = salaryCreditDate; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
