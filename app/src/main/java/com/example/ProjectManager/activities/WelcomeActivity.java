package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;

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
        btnSkip.setOnClickListener(v -> previousStep());
    }

    private void nextStep() {
        if (step < 4) {
            step++;
            updateUI();
        } else {
            // Sign In
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void previousStep() {
        if (step == 4){
            // Sign In
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        } else if (step > 1){
            step--;
            updateUI();
        }
    }

    private void updateUI() {
        switch (step) {

            case 1:
                titleText.setText("Welcome to Syncro!");
                descText.setText("Organize your tasks, projects, and ideas all in one place. Stay productive, stay in sync.");
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(35);
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 2:
                titleText.setText("Your Work, Your Way");
                descText.setText("Create tasks and manage every project,\nbig or small.");
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(65);
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 3:
                titleText.setText("Teamwork Made Simple");
                descText.setText("Invite your team, assign tasks, and track progress together in real time.");
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(100);
                btnNext.setText("Next");
                btnSkip.setText("Skip");
                break;

            case 4:
                titleText.setText("Stay on Top of Deadlines");
                descText.setText("Set due dates, get reminders, and sync across all your devices.");
                btnNext.setText("Sign in");
                btnSkip.setText("Sign up");
                break;
        }
    }
}

