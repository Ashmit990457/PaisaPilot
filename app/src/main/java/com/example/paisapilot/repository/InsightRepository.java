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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InsightRepository {

    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;
    private final GoalDao goalDao;
    private final RecurringBillDao recurringBillDao;
    private final UserProfileDao profileDao;
    private final SessionManager sessionManager;

    public InsightRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.budgetDao = db.budgetDao();
        this.goalDao = db.goalDao();
        this.recurringBillDao = db.recurringBillDao();
        this.profileDao = db.userProfileDao();
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface InsightCallback {
        void onSuccess(@NonNull List<Insight> insights);
        void onError(@NonNull String message);
    }

    public LiveData<List<Insight>> getInsights() {
        String userId = sessionManager.getUserId();
        MediatorLiveData<List<Insight>> result = new MediatorLiveData<>();

        LiveData<UserProfileEntity> profileLive = profileDao.getUserProfile(userId);
        LiveData<List<ExpenseEntity>> expensesLive = expenseDao.getExpensesByUser(userId);
        LiveData<List<BudgetEntity>> budgetsLive = budgetDao.getBudgetsByUser(userId);
        LiveData<List<GoalEntity>> goalsLive = goalDao.getGoalsByUser(userId);
        LiveData<List<RecurringBillEntity>> recurringLive = recurringBillDao.getRecurringBillsByUser(userId);

        result.addSource(profileLive, p -> updateInsights(result, p, expensesLive.getValue(), budgetsLive.getValue(), goalsLive.getValue(), recurringLive.getValue()));
        result.addSource(expensesLive, e -> updateInsights(result, profileLive.getValue(), e, budgetsLive.getValue(), goalsLive.getValue(), recurringLive.getValue()));
        result.addSource(budgetsLive, b -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), b, goalsLive.getValue(), recurringLive.getValue()));
        result.addSource(goalsLive, g -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), budgetsLive.getValue(), g, recurringLive.getValue()));
        result.addSource(recurringLive, r -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), budgetsLive.getValue(), goalsLive.getValue(), r));

        return result;
    }

    private void updateInsights(MediatorLiveData<List<Insight>> result, UserProfileEntity profile, List<ExpenseEntity> expenses, List<BudgetEntity> budgets, List<GoalEntity> goals, List<RecurringBillEntity> recurring) {
        if (expenses == null || budgets == null || goals == null || recurring == null) return;

        UserProfile userProfile = profile != null ? Mapper.toModel(profile) : null;
        List<Expense> expenseModels = Mapper.toModelList(expenses);
        List<Budget> budgetModels = Mapper.toBudgetModelList(budgets);
        List<SavingsGoal> goalModels = Mapper.toGoalModelList(goals);
        List<RecurringExpense> recurringModels = Mapper.toRecurringModelList(recurring);

        List<Insight> insights = calculateInsights(userProfile, expenseModels, budgetModels, goalModels, recurringModels);
        result.setValue(insights);
    }

    private List<Insight> calculateInsights(UserProfile profile, List<Expense> expenses, List<Budget> budgets, List<SavingsGoal> goals, List<RecurringExpense> recurring) {
        List<Insight> insights = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        int lastWeek = currentWeek - 1;

        double currentMonthSpending = 0;
        double currentWeekSpending = 0;
        double lastWeekSpending = 0;
        Map<String, Double> categorySpending = new HashMap<>();

        for (Expense expense : expenses) {
            if (expense.getDate() == null) continue;
            Calendar expCal = Calendar.getInstance();
            expCal.setTime(expense.getDate().toDate());

            if (expCal.get(Calendar.MONTH) == currentMonth && expCal.get(Calendar.YEAR) == currentYear) {
                currentMonthSpending += expense.getAmount();
                String cat = expense.getCategory();
                categorySpending.put(cat, categorySpending.getOrDefault(cat, 0.0) + expense.getAmount());
                
                if (profile != null && expense.getAmount() > 0.2 * profile.getMonthlyIncome()) {
                    insights.add(new Insight(
                            "Large Purchase Detected",
                            String.format(Locale.getDefault(), "₹%.2f spent on %s is over 20%% of your income.", expense.getAmount(), expense.getTitle()),
                            Insight.Type.INFO,
                            7
                    ));
                }
            }

            if (expCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && expCal.get(Calendar.YEAR) == currentYear) {
                currentWeekSpending += expense.getAmount();
            } else if (expCal.get(Calendar.WEEK_OF_YEAR) == lastWeek && expCal.get(Calendar.YEAR) == currentYear) {
                lastWeekSpending += expense.getAmount();
            }
        }

        // 1. Budget Insights
        for (Budget budget : budgets) {
            double spent = budget.getSpentAmount();
            double limit = budget.getMonthlyLimit();
            if (spent > limit) {
                insights.add(new Insight(
                        "Budget Exceeded",
                        String.format(Locale.getDefault(), "You exceeded your %s budget by ₹%.2f.", budget.getCategory(), spent - limit),
                        Insight.Type.WARNING,
                        10
                ));
            } else if (spent > 0.9 * limit) {
                insights.add(new Insight(
                        "Budget Warning",
                        String.format(Locale.getDefault(), "%s budget is almost exhausted.", budget.getCategory()),
                        Insight.Type.WARNING,
                        8
                ));
            }
        }

        // 1.1 Savings Goal Insights
        for (SavingsGoal goal : goals) {
            if (goal.isCompleted()) {
                insights.add(new Insight(
                        "Goal Completed!",
                        String.format(Locale.getDefault(), "Congratulations! You reached your goal: %s.", goal.getTitle()),
                        Insight.Type.SUCCESS,
                        12
                ));
            } else if (goal.getPercentage() >= 90) {
                insights.add(new Insight(
                        "Almost There!",
                        String.format(Locale.getDefault(), "You're %d%% toward your %s goal.", goal.getPercentage(), goal.getTitle()),
                        Insight.Type.SUCCESS,
                        9
                ));
            } else {
                double remaining = goal.getRemainingAmount();
                insights.add(new Insight(
                        "Savings Tip",
                        String.format(Locale.getDefault(), "Try saving ₹%.2f more to reach your %s target.", remaining / 4, goal.getTitle()),
                        Insight.Type.TIP,
                        2
                ));
            }
        }

        // 1.2 Recurring Bill Insights
        int billsThisWeek = 0;
        Calendar weekEnd = Calendar.getInstance();
        weekEnd.add(Calendar.DAY_OF_YEAR, 7);

        for (RecurringExpense item : recurring) {
            if (item.getNextDueDate() == null) continue;
            Calendar due = Calendar.getInstance();
            due.setTime(item.getNextDueDate().toDate());

            if (due.after(cal) && due.before(weekEnd)) {
                billsThisWeek++;
                long daysUntil = (due.getTimeInMillis() - cal.getTimeInMillis()) / (24 * 60 * 60 * 1000);
                if (daysUntil <= 2) {
                    insights.add(new Insight(
                            "Bill Due Soon",
                            String.format(Locale.getDefault(), "%s payment due in %d days.", item.getTitle(), daysUntil),
                            Insight.Type.WARNING,
                            11
                    ));
                }
            }
        }
        if (billsThisWeek > 0) {
            insights.add(new Insight(
                    "Upcoming Bills",
                    String.format(Locale.getDefault(), "You have %d recurring bills this week.", billsThisWeek),
                    Insight.Type.INFO,
                    6
            ));
        }

        // 1.3 Financial Forecast Insights
        if (profile != null) {
            double incomeVal = profile.getMonthlyIncome();
            int todayDay = cal.get(Calendar.DAY_OF_MONTH);
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            double avgDaily = currentMonthSpending / Math.max(todayDay, 1);
            double projectedExpense = avgDaily * totalDays;
            double predictedSavings = incomeVal - projectedExpense;

            if (projectedExpense > incomeVal && incomeVal > 0) {
                insights.add(new Insight(
                        "Overspending Alert",
                        "You are projected to overspend your monthly income by month end.",
                        Insight.Type.WARNING,
                        15
                ));
            } else if (incomeVal > 0) {
                double savingsRatio = predictedSavings / incomeVal;
                if (savingsRatio > 0.2) {
                    insights.add(new Insight(
                            "Savings Track",
                            String.format(Locale.getDefault(), "Excellent! You are on track to save over ₹%.0f this month.", predictedSavings),
                            Insight.Type.SUCCESS,
                            7
                    ));
                }
            }
        }

        // 2. Savings Insight
        double totalBudgetLimit = 0;
        for (Budget b : budgets) totalBudgetLimit += b.getMonthlyLimit();
        if (totalBudgetLimit > 0 && currentMonthSpending < 0.7 * totalBudgetLimit) {
            insights.add(new Insight(
                    "Great Savings!",
                    "Great job! You are saving well this month.",
                    Insight.Type.SUCCESS,
                    6
            ));
        }

        // 3. Highest Category
        String highestCat = null;
        double maxSpending = -1;
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            if (entry.getValue() > maxSpending) {
                maxSpending = entry.getValue();
                highestCat = entry.getKey();
            }
        }
        if (highestCat != null) {
            insights.add(new Insight(
                    "Highest Category",
                    String.format("You spend most on %s.", highestCat),
                    Insight.Type.INFO,
                    5
            ));
            
            if (currentMonthSpending > 0 && categorySpending.get(highestCat) > 0.4 * currentMonthSpending) {
                insights.add(new Insight(
                        "Optimization Tip",
                        String.format("Consider reducing %s expenses. It's over 40%% of your monthly total.", highestCat),
                        Insight.Type.TIP,
                        4
                ));
            }
        }

        // 5. Weekly Trend
        if (lastWeekSpending > 0) {
            double percentChange = ((currentWeekSpending - lastWeekSpending) / lastWeekSpending) * 100;
            if (percentChange > 0) {
                insights.add(new Insight(
                        "Spending Increased",
                        String.format("You spent %.0f%% more this week compared to last.", percentChange),
                        Insight.Type.INFO,
                        3
                ));
            } else if (percentChange < 0) {
                insights.add(new Insight(
                        "Spending Decreased",
                        String.format("Great! Spending reduced by %.0f%% compared to last week.", Math.abs(percentChange)),
                        Insight.Type.SUCCESS,
                        3
                ));
            }
        }

        Collections.sort(insights, (a, b) -> b.getPriority() - a.getPriority());
        if (insights.size() > 5) {
            return insights.subList(0, 5);
        }
        return insights;
    }
}
