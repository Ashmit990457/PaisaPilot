package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.dao.*;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.*;
import com.example.paisapilot.utils.Mapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportRepository {

    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;
    private final GoalDao goalDao;
    private final RecurringBillDao recurringBillDao;
    private final UserProfileDao profileDao;
    private final ArchiveDao archiveDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ReportRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.budgetDao = db.budgetDao();
        this.goalDao = db.goalDao();
        this.recurringBillDao = db.recurringBillDao();
        this.profileDao = db.userProfileDao();
        this.archiveDao = db.archiveDao();
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface ReportCallback {
        void onSuccess(@NonNull MonthlyReport report);
        void onError(@NonNull String message);
    }

    public void loadReportForMonth(@NonNull String monthId, @NonNull ReportCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonthId = sdf.format(new java.util.Date());

        if (monthId.equals(currentMonthId)) {
            generateLiveMonthlyReport(callback);
        } else {
            fetchArchivedReport(userId, monthId, callback);
        }
    }

    private void generateLiveMonthlyReport(@NonNull ReportCallback callback) {
        String userId = sessionManager.getUserId();
        executor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfMonth = calendar.getTimeInMillis();
                String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + calendar.get(Calendar.YEAR);

                UserProfileEntity profileEntity = profileDao.getUserProfileSync(userId);
                if (profileEntity == null) {
                    callback.onError("Profile not found");
                    return;
                }

                List<ExpenseEntity> expenseEntities = expenseDao.getExpensesByUserAfterSync(userId, startOfMonth);
                List<BudgetEntity> budgetEntities = budgetDao.getBudgetsByUserSync(userId);
                List<GoalEntity> goalEntities = goalDao.getGoalsByUserSync(userId);
                List<RecurringBillEntity> recurringEntities = recurringBillDao.getRecurringBillsByUserSync(userId);

                MonthlyReport report = compileReport(
                        monthName,
                        profileEntity.getMonthlyIncome(),
                        Mapper.toModelList(expenseEntities),
                        Mapper.toBudgetModelList(budgetEntities),
                        Mapper.toGoalModelList(goalEntities),
                        Mapper.toRecurringModelList(recurringEntities)
                );
                callback.onSuccess(report);
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Report failed");
            }
        });
    }

    private void fetchArchivedReport(String userId, String monthId, ReportCallback callback) {
        executor.execute(() -> {
            MonthlyArchiveEntity entity = archiveDao.getArchiveByMonth(userId, monthId);
            if (entity != null) {
                MonthlyReport report = new MonthlyReport();
                report.setMonthName(formatMonthIdForDisplay(monthId));
                report.setTotalIncome(entity.getTotalIncome());
                report.setTotalExpenses(entity.getTotalExpenses());
                report.setTotalSavings(entity.getTotalSavings());
                report.setBudgetUtilization(entity.getBudgetUsagePercent());
                report.setAiSummary(entity.getAiInsightSummary());
                // Minimal archived data
                report.setAllExpenses(new ArrayList<>()); 
                callback.onSuccess(report);
            } else {
                callback.onError("No data available for " + formatMonthIdForDisplay(monthId));
            }
        });
    }

    private String formatMonthIdForDisplay(String monthId) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            java.util.Date date = in.parse(monthId);
            return date != null ? out.format(date) : monthId;
        } catch (Exception e) {
            return monthId;
        }
    }

    public LiveData<List<MonthlyArchiveEntity>> getAllArchives() {
        return archiveDao.getArchivesByUser(sessionManager.getUserId());
    }

    public MonthlyReport compileReport(String monthName, double income, List<Expense> expenses, List<Budget> budgets, List<SavingsGoal> goals, List<RecurringExpense> recurring) {
        MonthlyReport report = new MonthlyReport();
        report.setMonthName(monthName);
        report.setTotalIncome(income);
        report.setAllExpenses(expenses);
        report.setTransactionCount(expenses.size());

        double totalExpenses = 0;
        Expense largest = null;
        Map<String, Double> categoryMap = new HashMap<>();

        for (Expense e : expenses) {
            totalExpenses += e.getAmount();
            if (largest == null || e.getAmount() > largest.getAmount()) {
                largest = e;
            }
            categoryMap.put(e.getCategory(), categoryMap.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        report.setTotalExpenses(totalExpenses);
        report.setLargestExpense(largest);
        report.setTotalSavings(income - totalExpenses);

        String highestCat = "None";
        double maxCatSpending = -1;
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > maxCatSpending) {
                maxCatSpending = entry.getValue();
                highestCat = entry.getKey();
            }
        }
        report.setHighestCategory(highestCat);

        double totalBudgetLimit = 0;
        for (Budget b : budgets) totalBudgetLimit += b.getMonthlyLimit();
        if (totalBudgetLimit > 0) {
            report.setBudgetUtilization((totalExpenses / totalBudgetLimit) * 100);
        }

        report.setGoalProgress(goals);
        
        List<RecurringExpense> upcoming = new ArrayList<>();
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.DAY_OF_YEAR, 30);
        long thirtyDaysLater = nextMonth.getTimeInMillis();
        
        for (RecurringExpense re : recurring) {
            if (re.getNextDueDate() != null && re.getNextDueDate().toDate().getTime() >= System.currentTimeMillis() && re.getNextDueDate().toDate().getTime() <= thirtyDaysLater) {
                upcoming.add(re);
            }
        }
        report.setUpcomingBills(upcoming);

        StringBuilder ai = new StringBuilder();
        if (totalExpenses > income && income > 0) {
            ai.append("Caution: Your expenses exceeded your income this month. ");
        } else if (income > 0) {
            ai.append("Great job! You saved ₹").append(String.format(Locale.getDefault(), "%.2f", income - totalExpenses)).append(" this month. ");
        }
        
        if (report.getBudgetUtilization() > 100) {
            ai.append("You went over your overall budget. ");
        } else if (report.getBudgetUtilization() > 90) {
            ai.append("You were very close to your budget limit. ");
        }
        
        if (!highestCat.equals("None")) {
            ai.append("Your highest spending was in ").append(highestCat).append(". ");
        }
        
        report.setAiSummary(ai.toString());

        return report;
    }
}
