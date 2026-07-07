package com.example.paisapilot.data.session;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private static final String PREF_NAME = "PaisaPilotSettings";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_CURRENCY = "currency_code";
    private static final String KEY_DATE_FORMAT = "date_format";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static SettingsManager instance;

    private SettingsManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public int getThemeMode() {
        return pref.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        editor.putInt(KEY_THEME, mode);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public String getCurrency() {
        return pref.getString(KEY_CURRENCY, "INR");
    }

    public void setCurrency(String currency) {
        editor.putString(KEY_CURRENCY, currency);
        editor.apply();
    }

    public String getDateFormat() {
        return pref.getString(KEY_DATE_FORMAT, "dd/MM/yyyy");
    }

    public void setDateFormat(String format) {
        editor.putString(KEY_DATE_FORMAT, format);
        editor.apply();
    }
}
