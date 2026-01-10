package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.UserRequestDto;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Sign up screen where users can create a new account.
 * Uses real API registration.
 */
public class SignUpActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSignUp;
    private TextView tvSignInLink;

    private SharedPrefsManager prefsManager;
    private ApiService apiService;
    private boolean isSigningUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefsManager = SharedPrefsManager.getInstance(this);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvSignInLink = findViewById(R.id.tv_sign_in_link);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSignUp.setOnClickListener(v -> handleSignUp());

        tvSignInLink.setOnClickListener(v -> {
            // Navigate back to login
            finish();
        });
    }

    private void handleSignUp() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(firstName, lastName, email, password, confirmPassword)) {
            return;
        }

        // Prevent multiple submissions
        if (isSigningUp) {
            return;
        }

        isSigningUp = true;
        btnSignUp.setEnabled(false);

        // Build registration request
        UserRequestDto request = new UserRequestDto(firstName, lastName, email, password);

        // Make API call to register
        Call<UserResponseDto> call = apiService.register(request);
        call.enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    // Registration successful, navigate to login
                    Toast.makeText(SignUpActivity.this, R.string.signup_successful, Toast.LENGTH_SHORT).show();
                    // User must login with their new credentials
                    finish();
                } else {
                    isSigningUp = false;
                    btnSignUp.setEnabled(true);
                    handleSignUpError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                isSigningUp = false;
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUpActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handle errors from registration API call
     */
    private void handleSignUpError(Response<UserResponseDto> response) {
        String errorMessage = "Error creating account";
        if (response.code() == 400) {
            errorMessage = "Invalid input data";
        } else if (response.code() == 409) {
            errorMessage = "Email already registered";
            etEmail.setError("This email is already in use");
            etEmail.requestFocus();
        } else if (response.code() == 500) {
            errorMessage = "Server error - please try again";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private boolean validateInputs(String firstName, String lastName, String email,
            String password, String confirmPassword) {
        // Validate first name
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError(getString(R.string.error_first_name_required));
            etFirstName.requestFocus();
            return false;
        }

        // Validate last name
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError(getString(R.string.error_last_name_required));
            etLastName.requestFocus();
            return false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            etEmail.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_too_short));
            etPassword.requestFocus();
            return false;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
