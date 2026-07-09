package com.example.paisapilot;

import android.app.Application;
import android.util.Log;
import com.example.paisapilot.data.local.AppDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaisaPilotApp extends Application {
    private static final String TAG = "PaisaPilotApp";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate - Warming up database");
        
        // Pre-initialize database on background thread to prevent splash freeze
        executor.execute(() -> {
            try {
                AppDatabase.getInstance(this);
                Log.d(TAG, "Database warmed up successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to warm up database", e);
            }
        });
    }
}
