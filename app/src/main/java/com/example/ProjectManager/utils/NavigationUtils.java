package com.example.ProjectManager.utils;

import android.content.Context;
import android.content.Intent;
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

    private static final int ACTIVE_COLOR = 0xFF4F46E5; // Primary color
    private static final int INACTIVE_COLOR = 0xFF94A3B8; // nav_inactive color

    /**
     * Update navigation bar to show active state for the current page
     * 
     * @param navHome     Home navigation item
     * @param navProjects Projects navigation item
     * @param navTasks    Tasks navigation item
     * @param navProfile  Profile navigation item
     * @param currentPage Current page ("home", "projects", "tasks", or "profile")
     */
    public static void updateNavigation(LinearLayout navHome, LinearLayout navProjects, LinearLayout navTasks,
            LinearLayout navProfile, String currentPage) {
        // Reset all to inactive
        resetNavItem(navHome);
        resetNavItem(navProjects);
        resetNavItem(navTasks);
        resetNavItem(navProfile);

        // Set active state for current page
        switch (currentPage.toLowerCase()) {
            case "home":
                setNavItemActive(navHome);
                break;
            case "projects":
                setNavItemActive(navProjects);
                break;
            case "tasks":
                setNavItemActive(navTasks);
                break;
            case "profile":
                setNavItemActive(navProfile);
                break;
            default:
                break;
        }
    }

    /**
     * Reset a navigation item to inactive state
     */
    private static void resetNavItem(LinearLayout navItem) {
        if (navItem == null || navItem.getChildCount() < 2) {
            return;
        }

        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView label = (TextView) navItem.getChildAt(1);

        icon.setColorFilter(INACTIVE_COLOR);
        label.setTextColor(INACTIVE_COLOR);
        label.setTypeface(null, Typeface.NORMAL);
    }

    /**
     * Set a navigation item to active state
     */
    private static void setNavItemActive(LinearLayout navItem) {
        if (navItem == null || navItem.getChildCount() < 2) {
            return;
        }

        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView label = (TextView) navItem.getChildAt(1);

        icon.setColorFilter(ACTIVE_COLOR);
        label.setTextColor(ACTIVE_COLOR);
        label.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Setup navigation click listeners
     */
    public static void setupNavigationListeners(Context context, LinearLayout navHome, LinearLayout navProjects, 
            LinearLayout navTasks, LinearLayout navProfile, String currentPage) {
        
        if (navHome != null && !currentPage.equals("home")) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.ProjectManager.activities.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
        
        if (navProjects != null && !currentPage.equals("projects")) {
            navProjects.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.ProjectManager.activities.ProjectsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
        
        if (navTasks != null && !currentPage.equals("tasks")) {
            navTasks.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.ProjectManager.activities.MyTasksActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
        
        if (navProfile != null && !currentPage.equals("profile")) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.ProjectManager.activities.ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
    }

    /**
     * Setup navigation click listeners - overload without currentPage
     * Determines current page from context class name
     */
    public static void setupNavigationListeners(Context context, LinearLayout navHome, LinearLayout navProjects, 
            LinearLayout navTasks, LinearLayout navProfile) {
        String currentPage = "home";
        String className = context.getClass().getSimpleName();
        if (className.contains("Projects")) {
            currentPage = "projects";
        } else if (className.contains("MyTasks") || className.contains("Task")) {
            currentPage = "tasks";
        } else if (className.contains("Profile")) {
            currentPage = "profile";
        }
        setupNavigationListeners(context, navHome, navProjects, navTasks, navProfile, currentPage);
    }
}
