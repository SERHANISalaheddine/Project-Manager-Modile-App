package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;

public class WelcomeActivity extends AppCompatActivity {

    Button btnNext, btnSkip;
    TextView titleText, descText;
    ProgressBar progressBar;

    int step = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        titleText = findViewById(R.id.titleText);
        descText = findViewById(R.id.descText);
        progressBar = findViewById(R.id.progressBar);

        updateUI();

        btnNext.setOnClickListener(v -> nextStep());
        // Skip should skip forward (or sign up on last step)
        btnSkip.setOnClickListener(v -> skipOrSignUp());
    }

    private void nextStep() {
        if (step < 4) {
            step++;
            updateUI();
        } else {
            // Last step → Sign In
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /**
     * Step 1-3: Skip onboarding → jump to last step (4)
     * Step 4: Sign up
     */
    private void skipOrSignUp() {
        if (step < 4) {
            step = 4;
            updateUI();
        } else {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
    }

    private void updateUI() {
        // Progress for 4 steps
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(4);
        progressBar.setProgress(step);

        switch (step) {

            case 1:
                titleText.setText("Welcome to Syncro!");
                descText.setText("Organize your tasks, projects, and ideas all in one place. Stay productive, stay in sync.");
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 2:
                titleText.setText("Your Work, Your Way");
                descText.setText("Create tasks and manage every project,\nbig or small.");
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 3:
                titleText.setText("Teamwork Made Simple");
                descText.setText("Invite your team, assign tasks, and track progress together in real time.");
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 4:
                titleText.setText("Stay on Top of Deadlines");
                descText.setText("Set due dates, get reminders, and sync across all your devices.");
                btnNext.setText("Sign in");
                btnSkip.setText("Sign up");
                // Optional: hide progress bar on last screen
                // progressBar.setVisibility(View.GONE);
                break;
        }
    }
}
