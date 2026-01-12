package com.example.ProjectManager.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ProjectManager.R;

/**
 * Utility class for managing navigation bar state across activities
 */
public class NavigationUtils {

    /**
     * Update navigation bar to show active state for the current page
     * 
     * @param navHome     Home navigation item
     * @param navCalendar Calendar navigation item
     * @param navTasks    Tasks navigation item
     * @param navProfile  Profile navigation item
     * @param currentPage Current page ("home", "calendar", "tasks", or "profile")
     */
    public static void updateNavigation(LinearLayout navHome, LinearLayout navCalendar, LinearLayout navTasks,
            LinearLayout navProfile, String currentPage) {
        // Reset all to inactive (gray)
        resetNavItem(navHome, false);
        resetNavItem(navCalendar, false);
        resetNavItem(navTasks, false);
        resetNavItem(navProfile, false);

        // Set active state for current page
        switch (currentPage.toLowerCase()) {
            case "home":
                setNavItemActive(navHome, true);
                break;
            case "calendar":
                setNavItemActive(navCalendar, true);
                break;
            case "tasks":
                setNavItemActive(navTasks, true);
                break;
            case "profile":
                setNavItemActive(navProfile, true);
                break;
            default:
                break;
        }
    }

    /**
     * Overloaded method for backward compatibility (without profile)
     */
    public static void updateNavigation(LinearLayout navHome, LinearLayout navCalendar, LinearLayout navTasks,
            String currentPage) {
        updateNavigation(navHome, navCalendar, navTasks, null, currentPage);
    }

    /**
     * Set a navigation item to active state (purple color, bold text)
     */
    private static void setNavItemActive(LinearLayout navItem, boolean isActive) {
        if (navItem == null || navItem.getChildCount() < 2) {
            return;
        }

        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView label = (TextView) navItem.getChildAt(1);

        if (isActive) {
            icon.setColorFilter(Color.parseColor("#7B61FF"));
            label.setTextColor(Color.parseColor("#7B61FF"));
            label.setTypeface(null, Typeface.BOLD);
        } else {
            icon.setColorFilter(Color.parseColor("#B4B4B4"));
            label.setTextColor(Color.parseColor("#B4B4B4"));
            label.setTypeface(null, Typeface.NORMAL);
        }
    }

    /**
     * Reset a navigation item to inactive state (gray color, normal text)
     */
    private static void resetNavItem(LinearLayout navItem, boolean isActive) {
        setNavItemActive(navItem, isActive);
    }
}
