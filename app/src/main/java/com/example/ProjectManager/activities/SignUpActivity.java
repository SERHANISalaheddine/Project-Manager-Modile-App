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
import com.example.ProjectManager.utils.SharedPrefsManager;

/**
 * Sign up screen where users can create a new account.
 * Uses mock registration for now.
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

    // Mock data for new user
    private static final long MOCK_NEW_USER_ID = 2L;
    private static final String MOCK_TOKEN = "mock_jwt_token_67890";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefsManager = SharedPrefsManager.getInstance(this);

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

        // TODO: Replace with API call - apiService.register(userRequestDto)
        // Call: RetrofitClient.getInstance().create(ApiService.class).register(new
        // UserRequestDto(firstName, lastName, email, password))
        // On success: show success message and navigate to login or auto-login
        // On error: show error message (e.g., email already exists)

        // Mock registration for now
        performMockSignUp(firstName, lastName, email);
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

    /**
     * Mock sign up implementation for testing without backend.
     * Replace this with actual API call when backend is ready.
     */
    private void performMockSignUp(String firstName, String lastName, String email) {
        // Mock registration: automatically log in the new user
        prefsManager.saveUserData(
                MOCK_NEW_USER_ID,
                email,
                firstName,
                lastName,
                MOCK_TOKEN);

        // Show success message
        Toast.makeText(this, R.string.signup_successful, Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity
        navigateToMain();
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
