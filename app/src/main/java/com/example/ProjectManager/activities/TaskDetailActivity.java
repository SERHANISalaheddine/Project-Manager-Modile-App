package com.example.ProjectManager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.Constants;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Task Detail Activity - view and update task status.
 */
public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private ImageView btnBack;
    private ImageView btnDelete;
    private TextView txtTaskName;
    private TextView txtTaskDescription;
    private TextView txtProjectName;
    private TextView txtDueDate;
    private TextView txtAssigneeName;
    private ImageView imgAssigneeAvatar;
    private MaterialCardView cardStatus;
    private AutoCompleteTextView dropdownStatus;
    private MaterialButton btnSaveStatus;
    private View loadingOverlay;

    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private long taskId;
    private TaskResponse currentTask;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private String[] statusOptions = {"TODO", "IN_PROGRESS", "DONE", "ARCHIVED"};
    private String[] statusDisplayNames = {"To Do", "In Progress", "Done", "Archived"};
    private String selectedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupClickListeners();
        setupStatusDropdown();
        setupBackPressHandler();
        
        loadTaskDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete);
        txtTaskName = findViewById(R.id.txt_task_name);
        txtTaskDescription = findViewById(R.id.txt_task_description);
        txtProjectName = findViewById(R.id.txt_project_name);
        txtDueDate = findViewById(R.id.txt_due_date);
        txtAssigneeName = findViewById(R.id.txt_assignee_name);
        imgAssigneeAvatar = findViewById(R.id.img_assignee_avatar);
        cardStatus = findViewById(R.id.card_status);
        dropdownStatus = findViewById(R.id.dropdown_status);
        btnSaveStatus = findViewById(R.id.btn_save_status);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnSaveStatus.setOnClickListener(v -> saveStatus());
    }

    private void setupStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            statusDisplayNames
        );
        dropdownStatus.setAdapter(adapter);
        
        dropdownStatus.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = statusOptions[position];
            btnSaveStatus.setEnabled(true);
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void loadTaskDetails() {
        showLoading(true);

        apiService.getTask(taskId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentTask = response.body();
                    displayTaskInfo();
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Failed to load task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(TaskDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTaskInfo() {
        txtTaskName.setText(currentTask.getName());

        String description = currentTask.getDescription();
        if (description != null && !description.isEmpty()) {
            txtTaskDescription.setText(description);
            txtTaskDescription.setVisibility(View.VISIBLE);
        } else {
            txtTaskDescription.setVisibility(View.GONE);
        }

        // Set current status in dropdown
        selectedStatus = currentTask.getStatus();
        int statusIndex = getStatusIndex(selectedStatus);
        if (statusIndex >= 0) {
            dropdownStatus.setText(statusDisplayNames[statusIndex], false);
        }

        // Load project info
        if (currentTask.getProjectId() > 0) {
            apiService.getProject(currentTask.getProjectId()).enqueue(new Callback<com.example.ProjectManager.models.dto.ProjectResponse>() {
                @Override
                public void onResponse(@NonNull Call<com.example.ProjectManager.models.dto.ProjectResponse> call,
                                       @NonNull Response<com.example.ProjectManager.models.dto.ProjectResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        txtProjectName.setText(response.body().getName());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<com.example.ProjectManager.models.dto.ProjectResponse> call, @NonNull Throwable t) {}
            });
        }

        // Load assignee info
        if (currentTask.hasAssigneeDetails()) {
            // Use assignee info directly from task response (RichTaskResponse)
            TaskResponse.Assignee assignee = currentTask.getAssignee();
            String name = (assignee.getFirstName() + " " + assignee.getLastName()).trim();
            txtAssigneeName.setText(name.isEmpty() ? "Unknown" : name);
            
            // Load avatar using assignee ID
            String avatarUrl = Constants.BASE_URL + "/api/v1/users/" + assignee.getId() + "/profile-picture";
            Glide.with(TaskDetailActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imgAssigneeAvatar);
        } else if (currentTask.getAssigneeId() != null) {
            // Backend returned only userId (TaskResponse), fetch user details
            loadAssigneeInfo(currentTask.getAssigneeId());
        } else {
            txtAssigneeName.setText("Unassigned");
            imgAssigneeAvatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Due date - not available from current backend
        txtDueDate.setText("No due date");

        btnSaveStatus.setEnabled(false);
    }

    private void loadAssigneeInfo(Long assigneeId) {
        apiService.getUser(assigneeId).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    String name = (user.getFirstName() + " " + user.getLastName()).trim();
                    txtAssigneeName.setText(name.isEmpty() ? "Unknown" : name);

                    // Load avatar
                    String avatarUrl = Constants.BASE_URL + "/api/v1/users/" + assigneeId + "/profile-picture";
                    Glide.with(TaskDetailActivity.this)
                        .load(avatarUrl)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(imgAssigneeAvatar);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                txtAssigneeName.setText("Unknown");
            }
        });
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(status)) {
                return i;
            }
        }
        return -1;
    }

    private void saveStatus() {
        if (selectedStatus == null || selectedStatus.equals(currentTask.getStatus())) {
            return;
        }

        showLoading(true);

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", selectedStatus);

        apiService.updateTaskStatus(taskId, statusMap).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(TaskDetailActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                    currentTask = response.body();
                    btnSaveStatus.setEnabled(false);
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(TaskDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete", (dialog, which) -> deleteTask())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteTask() {
        showLoading(true);

        apiService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(TaskDetailActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(TaskDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
