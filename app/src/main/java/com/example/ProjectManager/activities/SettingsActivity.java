package com.example.ProjectManager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.ProjectManager.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings Activity - app preferences and about section.
 */
public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotifications;
    private LinearLayout menuAbout;
    private LinearLayout menuPrivacyPolicy;
    private LinearLayout menuTerms;
    private LinearLayout menuRateApp;
    private TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupClickListeners();
        loadSettings();
        setupBackPressHandler();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchNotifications = findViewById(R.id.switch_notifications);
        menuAbout = findViewById(R.id.menu_about);
        menuPrivacyPolicy = findViewById(R.id.menu_privacy_policy);
        menuTerms = findViewById(R.id.menu_terms);
        menuRateApp = findViewById(R.id.menu_rate_app);
        txtVersion = findViewById(R.id.txt_version);
        
        // Set version
        try {
            String versionName = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
            txtVersion.setText("Version " + versionName);
        } catch (Exception e) {
            txtVersion.setText("Version 1.0");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Save preference
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("dark_mode", isChecked)
                .apply();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("notifications_enabled", isChecked)
                .apply();
        });

        menuAbout.setOnClickListener(v -> {
            // Show about dialog or activity
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About Project Manager")
                .setMessage("Project Manager is a modern task and project management application.\n\n" +
                    "Built with ❤️ by your team.\n\n" +
                    "© 2024 All rights reserved.")
                .setPositiveButton("OK", null)
                .show();
        });

        menuPrivacyPolicy.setOnClickListener(v -> {
            // Open privacy policy URL
            Intent intent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://example.com/privacy"));
            startActivity(intent);
        });

        menuTerms.setOnClickListener(v -> {
            // Open terms URL
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://example.com/terms"));
            startActivity(intent);
        });

        menuRateApp.setOnClickListener(v -> {
            // Open Play Store
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
            }
        });
    }

    private void loadSettings() {
        // Load saved settings
        boolean darkMode = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false);
        boolean notifications = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("notifications_enabled", true);

        switchDarkMode.setChecked(darkMode);
        switchNotifications.setChecked(notifications);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
}
