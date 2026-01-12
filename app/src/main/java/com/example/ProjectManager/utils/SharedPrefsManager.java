package com.example.ProjectManager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages shared preferences for user authentication and app settings.
 */
public class SharedPrefsManager {
    private static final String PREFS_NAME = "ProjectManagerPrefs";

    // Keys for authentication
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_FIRST_NAME = "user_first_name";
    private static final String KEY_USER_LAST_NAME = "user_last_name";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_LAST_PROJECT_ID = "last_project_id";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private static SharedPrefsManager instance;

    /**
     * Get singleton instance
     */
    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    private SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Save user login information
     */
    public void saveUserData(long userId, String email, String firstName, String lastName, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FIRST_NAME, firstName);
        editor.putString(KEY_USER_LAST_NAME, lastName);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user ID
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get user first name
     */
    public String getUserFirstName() {
        return prefs.getString(KEY_USER_FIRST_NAME, "");
    }

    /**
     * Get user last name
     */
    public String getUserLastName() {
        return prefs.getString(KEY_USER_LAST_NAME, "");
    }

    /**
     * Get full name
     */
    public String getUserFullName() {
        String firstName = getUserFirstName();
        String lastName = getUserLastName();
        return (firstName + " " + lastName).trim();
    }

    /**
     * Get auth token
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    /**
     * Set remember me preference
     */
    public void setRememberMe(boolean remember) {
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        editor.apply();
    }

    /**
     * Get remember me preference
     */
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Clear all user data (logout)
     */
    public void clearUserData() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_FIRST_NAME);
        editor.remove(KEY_USER_LAST_NAME);
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_REMEMBER_ME);
        editor.apply();
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    /**
     * Save the last opened project ID
     */
    public void saveLastProjectId(long projectId) {
        editor.putLong(KEY_LAST_PROJECT_ID, projectId);
        editor.apply();
    }

    /**
     * Get the last opened project ID
     */
    public long getLastProjectId() {
        return prefs.getLong(KEY_LAST_PROJECT_ID, -1);
    }
}
