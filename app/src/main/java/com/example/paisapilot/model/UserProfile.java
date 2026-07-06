package com.example.paisapilot.model;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

/**
 * UserProfile
 *
 * Immutable model representing a user's profile collected during setup.
 */
public class UserProfile {
    private final String fullName;
    private final String occupation;
    private final String city;
    private final double monthlyIncome;
    private final double monthlySavingGoal;
    private final String currency;
    private final int salaryCreditDate; // day of month (1-31)

    public UserProfile(@NonNull String fullName,
                       @NonNull String occupation,
                       @NonNull String city,
                       double monthlyIncome,
                       double monthlySavingGoal,
                       @NonNull String currency,
                       int salaryCreditDate) {
        this.fullName = fullName;
        this.occupation = occupation;
        this.city = city;
        this.monthlyIncome = monthlyIncome;
        this.monthlySavingGoal = monthlySavingGoal;
        this.currency = currency;
        this.salaryCreditDate = salaryCreditDate;
    }

    public String getFullName() { return fullName; }
    public String getOccupation() { return occupation; }
    public String getCity() { return city; }
    public double getMonthlyIncome() { return monthlyIncome; }
    public double getMonthlySavingGoal() { return monthlySavingGoal; }
    public String getCurrency() { return currency; }
    public int getSalaryCreditDate() { return salaryCreditDate; }

    /**
     * Convert to a Map for Firestore storage.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("fullName", fullName);
        m.put("occupation", occupation);
        m.put("city", city);
        m.put("monthlyIncome", monthlyIncome);
        m.put("monthlySavingGoal", monthlySavingGoal);
        m.put("currency", currency);
        m.put("salaryCreditDate", salaryCreditDate);
        return m;
    }
}
