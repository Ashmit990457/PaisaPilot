package com.example.paisapilot.data.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "PaisaPilotSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_FIRST_LAUNCH = "is_first_launch";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private static SessionManager instance;

    private SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setLogin(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setFirstLaunch(boolean isFirst) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirst);
        editor.apply();
    }

    public boolean isFirstLaunch() {
        return pref.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
