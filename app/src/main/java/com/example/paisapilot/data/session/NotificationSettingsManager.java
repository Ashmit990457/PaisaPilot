package com.example.paisapilot.data.session;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationSettingsManager {
    private static final String PREF_NAME = "PaisaPilotNotifications";
    
    public static final String KEY_BUDGET_ALERTS = "budget_alerts";
    public static final String KEY_BUDGET_80_WARNING = "budget_80_warning";
    public static final String KEY_BUDGET_EXCEEDED = "budget_exceeded";
    
    public static final String KEY_GOAL_PROGRESS = "goal_progress";
    public static final String KEY_GOAL_COMPLETED = "goal_completed";
    
    public static final String KEY_RECURRING_REMINDER = "recurring_reminder";
    
    public static final String KEY_DAILY_SUMMARY = "daily_summary";
    public static final String KEY_DAILY_SUMMARY_TIME = "daily_summary_time";
    
    public static final String KEY_WEEKLY_SUMMARY = "weekly_summary";
    public static final String KEY_MONTHLY_SUMMARY = "monthly_summary";
    
    public static final String KEY_AI_INSIGHTS = "ai_insights";
    public static final String KEY_EXPENSE_REMINDER = "expense_reminder";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static NotificationSettingsManager instance;

    private NotificationSettingsManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized NotificationSettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationSettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    // Generic Getters/Seters
    public boolean isEnabled(String key, boolean defaultValue) {
        return pref.getBoolean(key, defaultValue);
    }

    public void setEnabled(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return pref.getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
    
    // Tracking notified states to prevent duplicates
    public boolean wasNotified(String tag) {
        return pref.getBoolean("notified_" + tag, false);
    }
    
    public void markNotified(String tag) {
        editor.putBoolean("notified_" + tag, true);
        editor.apply();
    }
    
    public void clearNotifiedTags() {
        // This can be used during monthly reset or similar
        SharedPreferences.Editor cleaner = pref.edit();
        for (String key : pref.getAll().keySet()) {
            if (key.startsWith("notified_")) {
                cleaner.remove(key);
            }
        }
        cleaner.apply();
    }
}
