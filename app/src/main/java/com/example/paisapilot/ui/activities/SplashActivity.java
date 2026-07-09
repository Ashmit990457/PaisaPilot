package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import android.util.Log;
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
import com.example.paisapilot.worker.RecurringExpenseWorker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Splash screen starting");
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        viewModel.getState().observe(this, resource -> {
            if (resource == null) return;
            Log.d(TAG, "observe: Status=" + resource.getStatus());
            
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progress.setVisibility(android.view.View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(android.view.View.GONE);
                    NavigationTarget target = resource.getData();
                    navigateTo(target);
                    break;
                case ERROR:
                    binding.progress.setVisibility(android.view.View.GONE);
                    Log.e(TAG, "Error: " + resource.getMessage());
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    // Fallback to Login on critical failure
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                    break;
            }
        });

        viewModel.start();

        // These background tasks should not block the main thread or navigation
        new Thread(() -> {
            Log.d(TAG, "Background: Checking rollover and scheduling...");
            new LifecycleManager(this).checkAndRunRollover();
            com.example.paisapilot.data.local.NotificationScheduler.scheduleAll(this);
            scheduleBackgroundTasks();
        }).start();
    }

    private void navigateTo(NavigationTarget target) {
        Log.d(TAG, "navigateTo: " + target);
        Intent intent;
        if (target == NavigationTarget.LOGIN) {
            intent = new Intent(this, LoginActivity.class);
        } else if (target == NavigationTarget.PROFILE_SETUP) {
            intent = new Intent(this, ProfileSetupActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
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
                15, TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CloudSyncPeriodic",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
        );
    }
}
