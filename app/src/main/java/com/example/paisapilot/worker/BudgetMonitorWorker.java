package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.BudgetEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetMonitorWorker extends Worker {

    public BudgetMonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        if (!nsm.isEnabled(NotificationSettingsManager.KEY_BUDGET_ALERTS, true)) return Result.success();

        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        List<BudgetEntity> budgets = db.budgetDao().getBudgetsByUserSync(userId);
        String monthId = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        for (BudgetEntity budget : budgets) {
            double percent = (budget.getSpentAmount() / budget.getMonthlyLimit()) * 100;
            
            if (percent >= 100 && nsm.isEnabled(NotificationSettingsManager.KEY_BUDGET_EXCEEDED, true)) {
                String tag = "budget_" + budget.getBudgetId() + "_" + monthId + "_100";
                if (!nsm.wasNotified(tag)) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_BUDGET, 
                            budget.getBudgetId().hashCode(), "Budget Exceeded", 
                            "You've exceeded your " + budget.getCategory() + " budget by ₹" + String.format(Locale.getDefault(), "%.0f", budget.getSpentAmount() - budget.getMonthlyLimit()));
                    nsm.markNotified(tag);
                }
            } else if (percent >= 80 && nsm.isEnabled(NotificationSettingsManager.KEY_BUDGET_80_WARNING, true)) {
                String tag = "budget_" + budget.getBudgetId() + "_" + monthId + "_80";
                if (!nsm.wasNotified(tag)) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_BUDGET, 
                            budget.getBudgetId().hashCode(), "Budget Warning", 
                            "Your " + budget.getCategory() + " budget is 80% utilized.");
                    nsm.markNotified(tag);
                }
            }
        }

        return Result.success();
    }
}
