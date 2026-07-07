package com.example.paisapilot.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paisapilot.data.session.NotificationSettingsManager;
import com.example.paisapilot.databinding.ActivityNotificationSettingsBinding;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationSettingsActivity extends BaseActivity {

    private ActivityNotificationSettingsBinding binding;
    private NotificationSettingsManager nsm;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications are disabled. You won't receive any alerts.", Toast.LENGTH_LONG).show();
                    disableAllSwitches();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.notifSettingsRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarNotifSettings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarNotifSettings.setNavigationOnClickListener(v -> finish());
        }

        nsm = NotificationSettingsManager.getInstance(this);

        setupSwitches();
        checkPermission();
    }

    private void setupSwitches() {
        bindSwitch(binding.switchBudget80, NotificationSettingsManager.KEY_BUDGET_80_WARNING);
        bindSwitch(binding.switchBudgetExceeded, NotificationSettingsManager.KEY_BUDGET_EXCEEDED);
        bindSwitch(binding.switchGoalProgress, NotificationSettingsManager.KEY_GOAL_PROGRESS);
        bindSwitch(binding.switchGoalCompleted, NotificationSettingsManager.KEY_GOAL_COMPLETED);
        bindSwitch(binding.switchDailySummary, NotificationSettingsManager.KEY_DAILY_SUMMARY);
        bindSwitch(binding.switchWeeklySummary, NotificationSettingsManager.KEY_WEEKLY_SUMMARY);
        bindSwitch(binding.switchMonthlySummary, NotificationSettingsManager.KEY_MONTHLY_SUMMARY);
        bindSwitch(binding.switchExpenseReminder, NotificationSettingsManager.KEY_EXPENSE_REMINDER);
        bindSwitch(binding.switchAIInsights, NotificationSettingsManager.KEY_AI_INSIGHTS);
    }

    private void bindSwitch(MaterialSwitch sw, String key) {
        sw.setChecked(nsm.isEnabled(key, true));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> nsm.setEnabled(key, isChecked));
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void disableAllSwitches() {
        binding.switchBudget80.setEnabled(false);
        binding.switchBudgetExceeded.setEnabled(false);
        binding.switchGoalProgress.setEnabled(false);
        binding.switchGoalCompleted.setEnabled(false);
        binding.switchDailySummary.setEnabled(false);
        binding.switchWeeklySummary.setEnabled(false);
        binding.switchMonthlySummary.setEnabled(false);
        binding.switchExpenseReminder.setEnabled(false);
        binding.switchAIInsights.setEnabled(false);
    }
}
