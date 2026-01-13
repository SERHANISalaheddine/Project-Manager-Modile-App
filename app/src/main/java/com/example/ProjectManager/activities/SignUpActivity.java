package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.ProjectManager.models.dto.MessageResponse;
import com.example.ProjectManager.models.dto.UserRequestDto;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private ImageButton btnTogglePassword;
    private ImageButton btnToggleConfirmPassword;

    private Button btnSignUp;
    private TextView tvSignInLink;

    private SharedPrefsManager prefsManager;
    private ApiService apiService;
    private boolean isSigningUp = false;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefsManager = SharedPrefsManager.getInstance(this);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        setupBackPressHandler();
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

        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnToggleConfirmPassword = findViewById(R.id.btn_toggle_confirm_password);

        btnSignUp = findViewById(R.id.btn_sign_up);
        tvSignInLink = findViewById(R.id.tv_sign_in_link);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        btnTogglePassword.setOnClickListener(v -> togglePassword());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPassword());

        btnSignUp.setOnClickListener(v -> handleSignUp());

        tvSignInLink.setOnClickListener(v -> {
            // Go to Login screen (better than finish, in case you came from onboarding)
            startActivity(new Intent(this, LoginActivity.class));
            // finish();
        });
    }

    private void togglePassword() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            etPassword.setTransformationMethod(null);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            etPassword.setTransformationMethod(
                    android.text.method.PasswordTransformationMethod.getInstance()
            );
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }

        etPassword.setSelection(etPassword.getText().length());
    }


    private void toggleConfirmPassword() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(null);
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
        } else {
            etConfirmPassword.setTransformationMethod(
                    android.text.method.PasswordTransformationMethod.getInstance()
            );
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
        }

        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }


    private void handleSignUp() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(firstName, lastName, email, password, confirmPassword)) {
            return;
        }

        if (isSigningUp) return;

        isSigningUp = true;
        btnSignUp.setEnabled(false);

        UserRequestDto request = new UserRequestDto(firstName, lastName, email, password);

        Call<UserResponseDto> call = apiService.register(request);
        call.enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    
                    // Show success dialog - email verification link was sent
                    showEmailSentDialog(user.getEmail());
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
                Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEmailSentDialog(String email) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Verify your email")
            .setMessage("A verification email has been sent to " + email + ". Please click the link in the email to activate your account.")
            .setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                // Navigate to login screen
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .setCancelable(false)
            .show();
    }

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

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError(getString(R.string.error_first_name_required));
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError(getString(R.string.error_last_name_required));
            etLastName.requestFocus();
            return false;
        }

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

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SignUpActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

}
