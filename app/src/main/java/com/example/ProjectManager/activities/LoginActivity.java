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

        // Mock authentication for now
        performMockLogin(email, password);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
