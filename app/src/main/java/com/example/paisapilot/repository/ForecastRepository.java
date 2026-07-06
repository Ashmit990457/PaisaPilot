package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.dao.ExpenseDao;
import com.example.paisapilot.data.local.dao.UserProfileDao;
import com.example.paisapilot.data.local.entity.ExpenseEntity;
import com.example.paisapilot.data.local.entity.UserProfileEntity;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.model.Forecast;
import com.example.paisapilot.utils.Mapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ForecastRepository {

    private final ExpenseDao expenseDao;
    private final UserProfileDao profileDao;
    private final SessionManager sessionManager;

    public ForecastRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.profileDao = db.userProfileDao();
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface ForecastCallback {
        void onSuccess(@NonNull Forecast forecast);
        void onError(@NonNull String message);
    }

    public LiveData<Forecast> getForecast() {
        String userId = sessionManager.getUserId();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        // This would require a MediatorLiveData to combine profile and expenses
        // But since DashboardRepository already does this, we might just keep the calculateForecast helper
        return null;
    }

    public Forecast calculateForecast(double income, List<Expense> expenses) {
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int totalDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        double totalExpenseThisMonth = 0;
        for (Expense e : expenses) {
            totalExpenseThisMonth += e.getAmount();
        }

        Forecast forecast = new Forecast();
        
        if (income <= 0) {
            forecast.setForecastMessage("Complete your profile to enable financial forecasting.");
        } else if (expenses.isEmpty()) {
            forecast.setAverageDailySpending(0);
            forecast.setProjectedExpense(0);
            forecast.setForecastedSavings(income);
            forecast.setRiskLevel(Forecast.RiskLevel.SAFE);
            forecast.setForecastMessage("Add your first expense to receive personalized financial forecasts.");
        } else {
            double averageDailySpending = totalExpenseThisMonth / Math.max(currentDay, 1);
            double projectedExpense = averageDailySpending * totalDaysInMonth;
            double forecastedSavings = income - projectedExpense;
            
            forecast.setAverageDailySpending(averageDailySpending);
            forecast.setProjectedExpense(projectedExpense);
            forecast.setForecastedSavings(forecastedSavings);
            
            double riskRatio = projectedExpense / income;
            if (riskRatio < 0.7) {
                forecast.setRiskLevel(Forecast.RiskLevel.SAFE);
                forecast.setForecastMessage(String.format(Locale.getDefault(), "You are on track to save approximately ₹%.2f this month.", forecastedSavings));
            } else if (riskRatio <= 0.9) {
                forecast.setRiskLevel(Forecast.RiskLevel.MODERATE);
                forecast.setForecastMessage("Your spending is increasing. Monitor expenses to stay within budget.");
            } else {
                forecast.setRiskLevel(Forecast.RiskLevel.HIGH);
                forecast.setForecastMessage("At your current spending rate you may exceed your monthly income.");
            }
        }
        return forecast;
    }
}
