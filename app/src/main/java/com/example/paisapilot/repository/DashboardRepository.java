package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.dao.*;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.*;
import com.example.paisapilot.utils.Mapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardRepository {

    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;
    private final UserProfileDao profileDao;
    private final SessionManager sessionManager;
    private final ForecastRepository forecastRepository;

    public DashboardRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.budgetDao = db.budgetDao();
        this.profileDao = db.userProfileDao();
        this.sessionManager = SessionManager.getInstance(context);
        this.forecastRepository = new ForecastRepository(context);
    }

    public interface DashboardCallback {
        void onSuccess(@NonNull DashboardData data);
        void onUserFetched(@NonNull String name);
        void onError(@NonNull String message);
    }

    public LiveData<String> getUserName() {
        String userId = sessionManager.getUserId();
        MediatorLiveData<String> result = new MediatorLiveData<>();
        result.addSource(profileDao.getUserProfile(userId), entity -> {
            if (entity != null) {
                result.setValue(entity.getFullName());
            } else {
                result.setValue("PaisaPilot User");
            }
        });
        return result;
    }

    public LiveData<DashboardData> getDashboardData() {
        String userId = sessionManager.getUserId();
        MediatorLiveData<DashboardData> result = new MediatorLiveData<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        LiveData<UserProfileEntity> profileLive = profileDao.getUserProfile(userId);
        LiveData<List<ExpenseEntity>> expensesLive = expenseDao.getExpensesByUserAfter(userId, startOfMonth);
        LiveData<List<BudgetEntity>> budgetsLive = budgetDao.getBudgetsByUser(userId);

        result.addSource(profileLive, profile -> updateDashboard(result, profile, expensesLive.getValue(), budgetsLive.getValue()));
        result.addSource(expensesLive, expenses -> updateDashboard(result, profileLive.getValue(), expenses, budgetsLive.getValue()));
        result.addSource(budgetsLive, budgets -> updateDashboard(result, profileLive.getValue(), expensesLive.getValue(), budgets));

        return result;
    }

    private void updateDashboard(MediatorLiveData<DashboardData> result, UserProfileEntity profile, List<ExpenseEntity> expenses, List<BudgetEntity> budgets) {
        if (profile == null || expenses == null || budgets == null) return;

        double totalExpenseThisMonth = 0;
        int expenseCount = expenses.size();
        Map<String, Double> categoryWiseExpenses = new HashMap<>();
        Map<Integer, Double> weeklyExpenses = new HashMap<>();

        List<Expense> expenseModels = new ArrayList<>();
        for (ExpenseEntity entity : expenses) {
            Expense model = Mapper.toModel(entity);
            expenseModels.add(model);
            
            totalExpenseThisMonth += model.getAmount();
            String category = model.getCategory();
            categoryWiseExpenses.put(category, categoryWiseExpenses.getOrDefault(category, 0.0) + model.getAmount());

            Calendar cal = Calendar.getInstance();
            if (model.getDate() != null) {
                cal.setTime(model.getDate().toDate());
                int week = cal.get(Calendar.WEEK_OF_MONTH);
                weeklyExpenses.put(week, weeklyExpenses.getOrDefault(week, 0.0) + model.getAmount());
            }
        }

        double monthlyIncome = profile.getMonthlyIncome();
        Forecast forecast = forecastRepository.calculateForecast(monthlyIncome, expenseModels);

        String highestCategory = "None";
        double maxSpending = -1;
        for (Map.Entry<String, Double> entry : categoryWiseExpenses.entrySet()) {
            if (entry.getValue() > maxSpending) {
                maxSpending = entry.getValue();
                highestCategory = entry.getKey();
            }
        }

        DashboardData data = new DashboardData(
                totalExpenseThisMonth,
                monthlyIncome,
                monthlyIncome - totalExpenseThisMonth,
                expenseCount,
                highestCategory,
                categoryWiseExpenses,
                weeklyExpenses,
                forecast
        );
        result.setValue(data);
    }
}
