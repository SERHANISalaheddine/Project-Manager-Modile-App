package com.example.ProjectManager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.ForgotPasswordRequest;
import com.example.ProjectManager.models.dto.MessageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendCode;
    private LinearLayout formLayout;
    private LinearLayout successLayout;
    private TextView tvSuccessMessage;
    private Button btnTryAgain;
    private Button btnBackToLogin;
    private ApiService apiService;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize API
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        btnSendCode = findViewById(R.id.btnSendCode);
        formLayout = findViewById(R.id.formLayout);
        successLayout = findViewById(R.id.successLayout);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendCode.setOnClickListener(v -> handleForgotPassword());
        
        if (btnTryAgain != null) {
            btnTryAgain.setOnClickListener(v -> showForm());
        }
        
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> finish());
        }
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoading) return;
        
        isLoading = true;
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        
        apiService.forgotPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                isLoading = false;
                btnSendCode.setEnabled(true);
                btnSendCode.setText("Send Reset Link");
                
                // Always show success to prevent email enumeration
                showSuccess();
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                isLoading = false;
                btnSendCode.setEnabled(true);
                btnSendCode.setText("Send Reset Link");
                
                // Always show success to prevent email enumeration
                showSuccess();
            }
        });
    }

    private void showSuccess() {
        if (formLayout != null) formLayout.setVisibility(View.GONE);
        if (successLayout != null) successLayout.setVisibility(View.VISIBLE);
        if (tvSuccessMessage != null) {
            tvSuccessMessage.setText("If your email is registered, you will receive a password reset link. Please check your inbox.");
        }
    }

    private void showForm() {
        if (successLayout != null) successLayout.setVisibility(View.GONE);
        if (formLayout != null) formLayout.setVisibility(View.VISIBLE);
        etEmail.setText("");
    }
}
