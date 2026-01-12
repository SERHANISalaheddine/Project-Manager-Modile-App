package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;

public class VerifyNewPasswordActivity extends AppCompatActivity {

    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_newpassword); // ton layout ci-dessus

        btnSignIn = findViewById(R.id.btn_sign_in);

        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(VerifyNewPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // pour ne pas revenir en arrière
        });
    }
}
