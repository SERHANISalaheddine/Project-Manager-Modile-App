package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.AuthResponseDto;
import com.example.ProjectManager.models.dto.LoginRequestDto;
import com.example.ProjectManager.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login screen where users can sign in with email/password.
 * Includes mock authentication for testing.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btnTogglePassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    private ScrollView scrollView;

    private TextView tvSignUpLink;

    private boolean isPasswordVisible = false;
    private SharedPrefsManager prefsManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsManager = SharedPrefsManager.getInstance(this);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        initViews();
        setupListeners();

        etEmail.setText(prefsManager.getUserEmail());
        cbRememberMe.setChecked(true);
    }

    private void initViews() {

        scrollView = findViewById(R.id.scroll_view);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnSignIn = findViewById(R.id.btn_sign_in);

        tvSignUpLink = findViewById(R.id.tv_sign_up_link);
    }

    private void setupListeners() {

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> handleLogin());

        tvSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Scroll to password field when focused (for keyboard appearance)
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && scrollView != null) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, etPassword.getBottom()));
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            // Show password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        isPasswordVisible = !isPasswordVisible;
        // Move cursor to end of text
        etPassword.setSelection(etPassword.getText().length());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading state
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");

        // Make API call to login endpoint
        Call<AuthResponseDto> call = apiService.login(new LoginRequestDto(email, password));
        call.enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.sign_in);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponseDto authResponse = response.body();
                    String token = authResponse.getToken();
                    Long userId = authResponse.getUserId();

                    // Save Remember Me preference
                    prefsManager.setRememberMe(cbRememberMe.isChecked());

                    // Save user data with userId from login response
                    // We'll fetch the full profile to get firstName and lastName
                    prefsManager.saveUserData(
                            userId != null ? userId : -1L,
                            email,
                            "", // firstName will be fetched separately if needed
                            "", // lastName will be fetched separately if needed
                            token);

                    // Fetch user profile to get full user details
                    if (userId != null) {
                        fetchUserProfile(userId);
                    }

                    // Show success message
                    Toast.makeText(LoginActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity
                    navigateToMain();
                } else {
                    // Handle error response
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable t) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.sign_in);

                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void handleLoginError(Response<AuthResponseDto> response) {
        if (response.code() == 401) {
            // Invalid credentials
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
            etPassword.setError(getString(R.string.error_invalid_credentials));
            etPassword.requestFocus();
        } else if (response.code() == 400) {
            // Bad request (validation error)
            Toast.makeText(this, "Invalid email or password format", Toast.LENGTH_LONG).show();
        } else if (response.code() == 403) {
            Toast.makeText(this, response.message(), Toast.LENGTH_LONG).show();
        } else {
            // Other errors
            Toast.makeText(this,
                    "Login failed with code: " + response.code() + " - " + response.message(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInputs(String email, String password) {
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

        return true;
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Fetch user profile to get full user details (firstName, lastName)
     */
    private void fetchUserProfile(Long userId) {
        Call<com.example.ProjectManager.models.dto.UserResponseDto> call = apiService.getUser(userId);
        call.enqueue(new Callback<com.example.ProjectManager.models.dto.UserResponseDto>() {
            @Override
            public void onResponse(Call<com.example.ProjectManager.models.dto.UserResponseDto> call,
                    Response<com.example.ProjectManager.models.dto.UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.ProjectManager.models.dto.UserResponseDto user = response.body();
                    // Update user data with full profile info
                    String token = prefsManager.getAuthToken();
                    prefsManager.saveUserData(
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            token);
                }
            }

            @Override
            public void onFailure(Call<com.example.ProjectManager.models.dto.UserResponseDto> call, Throwable t) {
                // Silently fail - user is still logged in, just without full profile
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}