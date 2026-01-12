package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.utils.SharedPrefsManager;

/**
 * Login screen where users can sign in with email/password.
 * Includes mock authentication for testing.
 */
public class LoginActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btnTogglePassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    private Button btnGoogleSignIn;
    private Button btnPhoneSignIn;
    private TextView tvSignUpLink;

    private boolean isPasswordVisible = false;
    private SharedPrefsManager prefsManager;

    // Mock credentials for testing
    private static final String MOCK_EMAIL = "test@example.com";
    private static final String MOCK_PASSWORD = "password123";
    private static final long MOCK_USER_ID = 1L;
    private static final String MOCK_FIRST_NAME = "Test";
    private static final String MOCK_LAST_NAME = "User";
    private static final String MOCK_TOKEN = "mock_jwt_token_12345";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupListeners();

        // Pre-fill email if Remember Me was checked
        if (prefsManager.isRememberMeEnabled()) {
            etEmail.setText(prefsManager.getUserEmail());
            cbRememberMe.setChecked(true);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnPhoneSignIn = findViewById(R.id.btn_phone_sign_in);
        tvSignUpLink = findViewById(R.id.tv_sign_up_link);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password flow
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        });

        btnSignIn.setOnClickListener(v -> handleLogin());

        btnGoogleSignIn.setOnClickListener(v -> {
            // TODO: Implement Google Sign In
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        });

        btnPhoneSignIn.setOnClickListener(v -> {
            // TODO: Implement Phone Sign In
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        });

        tvSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
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

        // TODO: Replace with API call - apiService.login(email, password)
        // Call: RetrofitClient.getInstance().create(ApiService.class).login(new
        // LoginRequestDto(email, password))
        // On success: save token and user data, navigate to MainActivity
        // On error: show error message

<<<<<<< Updated upstream
        // Mock authentication for now
        performMockLogin(email, password);
=======
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
        } else {
            // Other server error
            Toast.makeText(this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
        }
>>>>>>> Stashed changes
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

    /**
     * Mock login implementation for testing without backend.
     * Replace this with actual API call when backend is ready.
     */
    private void performMockLogin(String email, String password) {
        // Mock authentication: check hardcoded credentials
        if (email.equals(MOCK_EMAIL) && password.equals(MOCK_PASSWORD)) {
            // Save Remember Me preference
            prefsManager.setRememberMe(cbRememberMe.isChecked());

            // Save mock user data to SharedPreferences
            prefsManager.saveUserData(
                    MOCK_USER_ID,
                    email,
                    MOCK_FIRST_NAME,
                    MOCK_LAST_NAME,
                    MOCK_TOKEN);

            // Show success message
            Toast.makeText(this, R.string.login_successful, Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity
            navigateToMain();
        } else {
            // Show error for invalid credentials
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
            etPassword.setError(getString(R.string.error_invalid_credentials));
            etPassword.requestFocus();
        }
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
