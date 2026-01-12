package com.example.ProjectManager.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.MessageResponse;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.Constants;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Modern Profile Activity with statistics, settings menu and profile picture management.
 * Inspired by modern UI design patterns from the web app.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    
    // Views
    private CircleImageView profileImage;
    private ImageView btnEditPicture;
    private ImageView btnBack;
    private ImageView btnSettings;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private TextView txtProjectCount;
    private TextView txtTaskCount;
    private TextView txtCompletedCount;
    private LinearLayout menuEditProfile;
    private LinearLayout menuChangePassword;
    private LinearLayout menuNotifications;
    private LinearLayout menuLogout;
    private LinearLayout menuDeleteAccount;
    private View loadingOverlay;
    
    // API & Data
    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private long userId;
    
    // Activity Result Launcher for image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize API and prefs
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);
        userId = prefsManager.getUserId();
        
        initViews();
        setupImagePicker();
        setupClickListeners();
        setupBackPressHandler();
        
        loadUserData();
        loadStatistics();
    }
    
    private void initViews() {
        profileImage = findViewById(R.id.img_profile);
        btnEditPicture = findViewById(R.id.btn_edit_photo);
        btnBack = findViewById(R.id.btn_back);
        btnSettings = findViewById(R.id.btn_settings);
        txtUserName = findViewById(R.id.tv_name);
        txtUserEmail = findViewById(R.id.tv_email);
        txtProjectCount = findViewById(R.id.tv_projects_count);
        txtTaskCount = findViewById(R.id.tv_tasks_count);
        txtCompletedCount = findViewById(R.id.tv_completed_count);
        menuEditProfile = findViewById(R.id.menu_edit_profile);
        menuChangePassword = findViewById(R.id.menu_change_password);
        menuNotifications = findViewById(R.id.menu_notifications);
        menuLogout = findViewById(R.id.menu_logout);
        menuDeleteAccount = findViewById(R.id.menu_delete_account);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfilePicture(imageUri);
                    }
                }
            }
        );
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEditPicture.setOnClickListener(v -> showImagePickerDialog());
        profileImage.setOnClickListener(v -> showImagePickerDialog());
        
        menuEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
        
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        
        menuNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });
        
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        menuLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        menuDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
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
        // Load from SharedPreferences first (instant)
        String fullName = prefsManager.getUserFullName();
        String email = prefsManager.getUserEmail();
        
        txtUserName.setText(fullName.isEmpty() ? "User" : fullName);
        txtUserEmail.setText(email);
        
        // Load profile picture
        loadProfilePicture();
        
        // Refresh from API
        apiService.getUser(userId).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    String name = user.getFirstName() + " " + user.getLastName();
                    txtUserName.setText(name.trim().isEmpty() ? "User" : name.trim());
                    txtUserEmail.setText(user.getEmail());
                    
                    // Update SharedPreferences
                    prefsManager.saveUserData(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        prefsManager.getAuthToken()
                    );
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                // Keep cached data, no action needed
            }
        });
    }
    
    private void loadProfilePicture() {
        String profilePictureUrl = Constants.BASE_URL + "/api/v1/users/" + userId + "/profile-picture";
        
        Glide.with(this)
            .load(profilePictureUrl)
            .transform(new CircleCrop())
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(profileImage);
    }
    
    private void loadStatistics() {
        // Load projects count
        apiService.getProjectsByOwner(userId, 0, 1).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call, 
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int ownedProjects = (int) response.body().getTotalElements();
                    
                    // Also load member projects
                    apiService.getProjectsByMember(userId, 0, 1).enqueue(new Callback<PageResponse<ProjectResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call,
                                               @NonNull Response<PageResponse<ProjectResponse>> response) {
                            int memberProjects = 0;
                            if (response.isSuccessful() && response.body() != null) {
                                memberProjects = (int) response.body().getTotalElements();
                            }
                            txtProjectCount.setText(String.valueOf(ownedProjects + memberProjects));
                        }

                        @Override
                        public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                            txtProjectCount.setText(String.valueOf(ownedProjects));
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                txtProjectCount.setText("0");
            }
        });
        
        // Load tasks count
        apiService.getAllTasks(0, 1, userId, null, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call,
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtTaskCount.setText(String.valueOf(response.body().getTotalElements()));
                } else {
                    txtTaskCount.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                txtTaskCount.setText("0");
            }
        });
        
        // Load completed tasks count
        apiService.getAllTasks(0, 1, userId, null, "DONE").enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call,
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtCompletedCount.setText(String.valueOf(response.body().getTotalElements()));
                } else {
                    txtCompletedCount.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                txtCompletedCount.setText("0");
            }
        });
    }
    
    private void showImagePickerDialog() {
        String[] options = {"Choose from Gallery", "Remove Photo"};
        
        new AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkPermissionAndOpenGallery();
                } else {
                    deleteProfilePicture();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void checkPermissionAndOpenGallery() {
        // For Android 13+, we don't need storage permission for picking images
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            openImagePicker();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
            } else {
                openImagePicker();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to select image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    private void uploadProfilePicture(Uri imageUri) {
        showLoading(true);
        
        try {
            File file = createTempFileFromUri(imageUri);
            if (file == null) {
                showLoading(false);
                Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
                return;
            }
            
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            
            apiService.uploadProfilePicture(part).enqueue(new Callback<UserResponseDto>() {
                @Override
                public void onResponse(@NonNull Call<UserResponseDto> call, 
                                      @NonNull Response<UserResponseDto> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                        loadProfilePicture();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to upload picture", Toast.LENGTH_SHORT).show();
                    }
                    // Clean up temp file
                    file.delete();
                }

                @Override
                public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    file.delete();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File tempFile = File.createTempFile("profile_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void deleteProfilePicture() {
        showLoading(true);
        
        apiService.deleteProfilePicture().enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, 
                                  @NonNull Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile picture removed", Toast.LENGTH_SHORT).show();
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to remove picture", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void performLogout() {
        prefsManager.clearUserData();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This action cannot be undone. All your data will be permanently deleted. Are you sure?")
            .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteAccount() {
        showLoading(true);
        
        apiService.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                    performLogout();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadUserData();
        loadStatistics();
    }
}
