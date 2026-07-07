package com.example.paisapilot.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.entity.GoalEntity;
import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.utils.NotificationHelper;

import java.util.List;

public class GoalMonitorWorker extends Worker {

    public GoalMonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationSettingsManager nsm = NotificationSettingsManager.getInstance(context);
        SessionManager sm = SessionManager.getInstance(context);
        String userId = sm.getUserId();
        if (userId == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(context);
        List<GoalEntity> goals = db.goalDao().getGoalsByUserSync(userId);

        int[] thresholds = {25, 50, 75, 90, 100};

        for (GoalEntity goal : goals) {
            double percent = (goal.getSavedAmount() / goal.getTargetAmount()) * 100;
            
            for (int t : thresholds) {
                if (percent >= t) {
                    if (t == 100 && !nsm.isEnabled(NotificationSettingsManager.KEY_GOAL_COMPLETED, true)) continue;
                    if (t < 100 && !nsm.isEnabled(NotificationSettingsManager.KEY_GOAL_PROGRESS, true)) continue;

                    String tag = "goal_" + goal.getGoalId() + "_" + t;
                    if (!nsm.wasNotified(tag)) {
                        String title = t == 100 ? "Goal Achieved!" : "Goal Progress";
                        String msg = t == 100 ? "Congratulations! You've reached your goal: " + goal.getTitle() :
                                "You've completed " + t + "% of your " + goal.getTitle() + " goal.";
                        
                        NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_GOALS, 
                                goal.getGoalId().hashCode() + t, title, msg);
                        nsm.markNotified(tag);
                    }
                }
            }
        }

        return Result.success();
    }
}
