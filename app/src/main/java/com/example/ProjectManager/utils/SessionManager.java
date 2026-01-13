package com.example.ProjectManager.utils;

import android.content.Context;

/**
 * Session Manager - Wrapper around SharedPrefsManager for managing user sessions.
 * Provides a simpler interface for authentication operations.
 */
public class SessionManager {

    private final SharedPrefsManager prefsManager;

    public SessionManager(Context context) {
        this.prefsManager = SharedPrefsManager.getInstance(context);
    }

    /**
     * Get the current logged in user's ID
     * @return User ID or null if not logged in
     */
    public Long getUserId() {
        long userId = prefsManager.getUserId();
        return userId > 0 ? userId : null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefsManager.isLoggedIn();
    }

    /**
     * Get auth token
     */
    public String getAuthToken() {
        return prefsManager.getAuthToken();
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefsManager.getUserEmail();
    }

    /**
     * Get user full name
     */
    public String getUserFullName() {
        return prefsManager.getUserFullName();
    }

    /**
     * Save user session data
     */
    public void saveSession(long userId, String email, String firstName, String lastName, String token) {
        prefsManager.saveUserData(userId, email, firstName, lastName, token);
    }

    /**
     * Clear the session (logout)
     */
    public void clearSession() {
        prefsManager.clearUserData();
    }
}
