package com.example.ProjectManager.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.UpdateUserRequest;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Edit Profile Activity - allows users to update their name and email.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilEmail;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private MaterialButton btnSave;
    private View loadingOverlay;

    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);
        userId = prefsManager.getUserId();

        initViews();
        setupClickListeners();
        loadUserData();
        setupBackPressHandler();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tilFirstName = findViewById(R.id.til_first_name);
        tilLastName = findViewById(R.id.til_last_name);
        tilEmail = findViewById(R.id.til_email);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void loadUserData() {
        // Load from prefs first
        etFirstName.setText(prefsManager.getUserFirstName());
        etLastName.setText(prefsManager.getUserLastName());
        etEmail.setText(prefsManager.getUserEmail());

        // Refresh from API
        showLoading(true);
        apiService.getUser(userId).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etEmail.setText(user.getEmail());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                showLoading(false);
            }
        });
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validation
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);

        if (TextUtils.isEmpty(firstName)) {
            tilFirstName.setError("First name is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            tilLastName.setError("Last name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format");
            return;
        }

        showLoading(true);

        UpdateUserRequest request = new UpdateUserRequest(firstName, lastName, email);
        apiService.updateUser(userId, request).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    
                    // Update SharedPreferences
                    prefsManager.saveUserData(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        prefsManager.getAuthToken()
                    );
                    
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnSave.setEnabled(!show);
    }
}
