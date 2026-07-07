package com.example.paisapilot.ui.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paisapilot.databinding.ActivityNotificationTestBinding;
import com.example.paisapilot.utils.NotificationHelper;

public class NotificationTestActivity extends BaseActivity {

    private ActivityNotificationTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarNotifTest);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarNotifTest.setNavigationOnClickListener(v -> finish());
        }

        binding.btnTestBudget.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_BUDGET, 101, 
                "Budget Alert Test", "You've exceeded your Food budget by ₹500.")
        );

        binding.btnTestGoal.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_GOALS, 102, 
                "Goal Progress Test", "You've completed 75% of your Vacation Fund.")
        );

        binding.btnTestDaily.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_DAILY, 103, 
                "Daily Summary Test", "₹1,250 spent today. Food ₹600, Shopping ₹650.")
        );

        binding.btnTestWeekly.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_WEEKLY, 104, 
                "Weekly Summary Test", "You spent ₹8,400 this week. Highest spending: Rent.")
        );

        binding.btnTestRecurring.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_RECURRING, 105, 
                "Bill Reminder Test", "Your Electricity bill is due tomorrow.")
        );

        binding.btnTestAI.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_AI, 106, 
                "AI Insight Test", "You spent 15% less than last week! Great job!")
        );

        binding.btnTestReminder.setOnClickListener(v -> 
            NotificationHelper.showNotification(this, NotificationHelper.CHANNEL_GENERAL, 107, 
                "Expense Reminder Test", "Don't forget to record today's expenses.")
        );
    }
}
