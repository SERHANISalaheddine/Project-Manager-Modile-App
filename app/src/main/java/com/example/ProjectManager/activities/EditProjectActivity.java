package com.example.ProjectManager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.CreateProjectRequest;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProjectActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "extra_project_id";

    private ImageView btnBack, btnSave;
    private TextInputEditText editProjectName, editDescription;
    private TextView txtStartDate, txtEndDate;
    private MaterialButton btnStartDate, btnEndDate, btnSaveProject;
    private View loadingOverlay;

    private ApiService apiService;
    private long projectId;
    private ProjectResponse currentProject;

    private Date startDate, endDate;
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (projectId == -1) {
            Toast.makeText(this, "Invalid project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadProject();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        editProjectName = findViewById(R.id.edit_project_name);
        editDescription = findViewById(R.id.edit_description);
        txtStartDate = findViewById(R.id.txt_start_date);
        txtEndDate = findViewById(R.id.txt_end_date);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnSaveProject = findViewById(R.id.btn_save_project);
        loadingOverlay = findViewById(R.id.loading_overlay);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> saveProject());
        btnSaveProject.setOnClickListener(v -> saveProject());

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // Text change listeners for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        editProjectName.addTextChangedListener(textWatcher);
        editDescription.addTextChangedListener(textWatcher);
    }

    private void loadProject() {
        showLoading(true);
        apiService.getProject(projectId).enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProject = response.body();
                    displayProject();
                } else {
                    Toast.makeText(EditProjectActivity.this, "Failed to load project", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProjectResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProjectActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProject() {
        editProjectName.setText(currentProject.getName());
        editDescription.setText(currentProject.getDescription());

        // Parse and display dates
        if (currentProject.getStartDate() != null) {
            try {
                startDate = apiFormat.parse(currentProject.getStartDate());
                if (startDate != null) {
                    txtStartDate.setText(displayFormat.format(startDate));
                }
            } catch (ParseException e) {
                txtStartDate.setText(currentProject.getStartDate());
            }
        }

        if (currentProject.getEndDate() != null) {
            try {
                endDate = apiFormat.parse(currentProject.getEndDate());
                if (endDate != null) {
                    txtEndDate.setText(displayFormat.format(endDate));
                }
            } catch (ParseException e) {
                txtEndDate.setText(currentProject.getEndDate());
            }
        }

        validateForm();
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = isStartDate ? startDate : endDate;
        if (currentDate != null) {
            calendar.setTime(currentDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    Date selectedDate = selected.getTime();

                    if (isStartDate) {
                        startDate = selectedDate;
                        txtStartDate.setText(displayFormat.format(selectedDate));
                    } else {
                        endDate = selectedDate;
                        txtEndDate.setText(displayFormat.format(selectedDate));
                    }
                    validateForm();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void validateForm() {
        String name = editProjectName.getText() != null ? editProjectName.getText().toString().trim() : "";
        boolean isValid = !name.isEmpty();
        btnSaveProject.setEnabled(isValid);
    }

    private void saveProject() {
        String name = editProjectName.getText() != null ? editProjectName.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Project name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        CreateProjectRequest request = new CreateProjectRequest(name, description);

        apiService.updateProject(projectId, request).enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditProjectActivity.this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProjectActivity.this, "Failed to update project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProjectResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProjectActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
