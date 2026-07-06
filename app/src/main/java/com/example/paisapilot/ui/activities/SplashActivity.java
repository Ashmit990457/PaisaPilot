package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.data.local.LifecycleManager;
import com.example.paisapilot.databinding.ActivitySplashBinding;
import com.example.paisapilot.model.NavigationTarget;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.viewmodel.SplashViewModel;
import com.example.paisapilot.ui.activities.LoginActivity;
import com.example.paisapilot.ui.activities.ProfileSetupActivity;
import com.example.paisapilot.ui.activities.MainActivity;
import com.example.paisapilot.worker.RecurringExpenseWorker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

/**
 * SplashActivity
 * <p>
 * Purpose: Brief entry screen shown at app startup. Shows app logo, name and a progress
 * indicator while the app performs initial work. No navigation or business logic is
 * implemented here per project requirements.
 */
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply system window insets to avoid overlapping system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Create ViewModel and start the splash flow which waits then checks auth/profile.
        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        viewModel.getState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progress.setVisibility(android.view.View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(android.view.View.GONE);
                    NavigationTarget target = resource.getData();
                    if (target == NavigationTarget.LOGIN) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    } else if (target == NavigationTarget.PROFILE_SETUP) {
                        startActivity(new Intent(SplashActivity.this, ProfileSetupActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                    break;
                case ERROR:
                    binding.progress.setVisibility(android.view.View.GONE);
                    // On error, show a Toast and fallback to Login
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                    break;
            }
        });

        viewModel.start();

        new LifecycleManager(this).checkAndRunRollover();

        scheduleBackgroundTasks();
    }

    private void scheduleBackgroundTasks() {
        PeriodicWorkRequest recurringWorkRequest = new PeriodicWorkRequest.Builder(
                RecurringExpenseWorker.class,
                24, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecurringExpenseWork",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWorkRequest
        );

        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                com.example.paisapilot.data.remote.SyncWorker.class,
                15, TimeUnit.MINUTES // Min periodic interval
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CloudSyncPeriodic",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
        );
    }
}
