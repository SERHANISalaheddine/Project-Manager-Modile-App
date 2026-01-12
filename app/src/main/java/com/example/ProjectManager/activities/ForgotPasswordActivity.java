package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.et_email);
        btnSendCode = findViewById(R.id.btnSendCode);

        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Message de confirmation
            Toast.makeText(this, "Verification code sent to " + email, Toast.LENGTH_SHORT).show();

            // Aller vers VerifyCodeActivity
            Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
            intent.putExtra("email", email); // optionnel si tu veux l'utiliser là-bas
            startActivity(intent);
        });
    }
}
