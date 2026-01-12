package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.utils.SharedPrefsManager;

/**
 * Onboarding/Splash screen shown on first app launch.
 * Provides options to Sign In or Sign Up.
 */
public class OnboardingActivity extends AppCompatActivity {

    private Button btnSignIn;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in with a valid token
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        String token = prefsManager.getAuthToken();
        if (prefsManager.isLoggedIn() && token != null && !token.isEmpty()) {
            // User is logged in with valid token, go directly to MainActivity
            navigateToMain();
            return;
        } else if (prefsManager.isLoggedIn()) {
            // User is marked as logged in but token is missing/invalid - clear and show
            // login
            prefsManager.clearUserData();
        }

        setContentView(R.layout.activity_onboarding);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignUp = findViewById(R.id.btn_sign_up);
    }

    private void setupListeners() {
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
