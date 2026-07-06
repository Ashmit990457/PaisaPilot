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
    private final ArchiveDao archiveDao;
    private final SessionManager sessionManager;

    public InsightRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.budgetDao = db.budgetDao();
        this.goalDao = db.goalDao();
        this.recurringBillDao = db.recurringBillDao();
        this.profileDao = db.userProfileDao();
        this.archiveDao = db.archiveDao();
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
        LiveData<List<MonthlyArchiveEntity>> archivesLive = archiveDao.getArchivesByUser(userId);

        result.addSource(profileLive, p -> updateInsights(result, p, expensesLive.getValue(), budgetsLive.getValue(), goalsLive.getValue(), recurringLive.getValue(), archivesLive.getValue()));
        result.addSource(expensesLive, e -> updateInsights(result, profileLive.getValue(), e, budgetsLive.getValue(), goalsLive.getValue(), recurringLive.getValue(), archivesLive.getValue()));
        result.addSource(budgetsLive, b -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), b, goalsLive.getValue(), recurringLive.getValue(), archivesLive.getValue()));
        result.addSource(goalsLive, g -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), budgetsLive.getValue(), g, recurringLive.getValue(), archivesLive.getValue()));
        result.addSource(recurringLive, r -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), budgetsLive.getValue(), goalsLive.getValue(), r, archivesLive.getValue()));
        result.addSource(archivesLive, a -> updateInsights(result, profileLive.getValue(), expensesLive.getValue(), budgetsLive.getValue(), goalsLive.getValue(), recurringLive.getValue(), a));

        return result;
    }

    private void updateInsights(MediatorLiveData<List<Insight>> result, UserProfileEntity profile, List<ExpenseEntity> expenses, List<BudgetEntity> budgets, List<GoalEntity> goals, List<RecurringBillEntity> recurring, List<MonthlyArchiveEntity> archives) {
        if (expenses == null || budgets == null || goals == null || recurring == null) return;

        UserProfile profileModel = profile != null ? Mapper.toModel(profile) : null;
        List<Expense> expModels = Mapper.toModelList(expenses);
        List<Budget> budModels = Mapper.toBudgetModelList(budgets);
        List<SavingsGoal> goalModels = Mapper.toGoalModelList(goals);
        List<RecurringExpense> recModels = Mapper.toRecurringModelList(recurring);

        List<Insight> insights = calculateInsights(profileModel, expModels, budModels, goalModels, recModels);
        
        // Add monthly comparison insight if archive exists
        if (archives != null && !archives.isEmpty()) {
            MonthlyArchiveEntity lastMonth = archives.get(0);
            double currentExpTotal = 0;
            // Filter only current month expenses for comparison
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            
            for (Expense e : expModels) {
                if (e.getDate() == null) continue;
                Calendar ec = Calendar.getInstance();
                ec.setTime(e.getDate().toDate());
                if (ec.get(Calendar.MONTH) == month && ec.get(Calendar.YEAR) == year) {
                    currentExpTotal += e.getAmount();
                }
            }
            
            double diff = lastMonth.getTotalExpenses() - currentExpTotal;
            if (diff > 0) {
                insights.add(0, new Insight("Monthly Savings", "You spent ₹" + String.format(Locale.getDefault(), "%.0f", diff) + " less than last month so far!", Insight.Type.SUCCESS, 15));
            } else if (diff < -1000) {
                insights.add(0, new Insight("Spending Spike", "You spent ₹" + String.format(Locale.getDefault(), "%.0f", Math.abs(diff)) + " more than last month already.", Insight.Type.WARNING, 15));
            }
        }

        Collections.sort(insights, (a, b) -> b.getPriority() - a.getPriority());
        if (insights.size() > 5) {
            result.setValue(insights.subList(0, 5));
        } else {
            result.setValue(insights);
        }
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
            }
        }

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

        if (profile != null) {
            double incomeVal = profile.getMonthlyIncome();
            int todayDay = cal.get(Calendar.DAY_OF_MONTH);
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            double projectedExpense = (currentMonthSpending / Math.max(todayDay, 1)) * totalDays;

            if (projectedExpense > incomeVal && incomeVal > 0) {
                insights.add(new Insight(
                        "Overspending Alert",
                        "You are projected to overspend your monthly income.",
                        Insight.Type.WARNING,
                        15
                ));
            }
        }

        if (lastWeekSpending > 0) {
            double percentChange = ((currentWeekSpending - lastWeekSpending) / lastWeekSpending) * 100;
            if (percentChange < 0) {
                insights.add(new Insight(
                        "Spending Decreased",
                        String.format("Great! Spending reduced by %.0f%% compared to last week.", Math.abs(percentChange)),
                        Insight.Type.SUCCESS,
                        3
                ));
            }
        }

        return insights;
    }
}
