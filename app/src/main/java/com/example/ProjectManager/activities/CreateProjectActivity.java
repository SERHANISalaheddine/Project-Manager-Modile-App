package com.example.ProjectManager.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.MemberAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.fragments.AddMemberBottomSheet;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.dto.CreateProjectRequest;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for creating a new project.
 * Allows users to enter project title, description, and select team members.
 */
public class CreateProjectActivity extends AppCompatActivity implements AddMemberBottomSheet.OnMembersSelectedListener {

    // UI Components
    private ImageButton btnBack;
    private EditText etProjectTitle;
    private EditText etProjectDescription;
    private LinearLayout layoutSelectMember;
    private TextView tvSelectedMembers;
    private RecyclerView rvSelectedMembers;
    private Button btnCreateProject;

    // Data
    private ArrayList<Member> selectedMembers;
    private MemberAdapter selectedMembersAdapter;
    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private boolean isCreating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        // Initialize data
        selectedMembers = new ArrayList<>();

        // Initialize API service and preferences
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();

        // Setup selected members RecyclerView
        setupSelectedMembersRecyclerView();
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etProjectTitle = findViewById(R.id.et_project_title);
        etProjectDescription = findViewById(R.id.et_project_description);
        layoutSelectMember = findViewById(R.id.layout_select_member);
        tvSelectedMembers = findViewById(R.id.tv_selected_members);
        rvSelectedMembers = findViewById(R.id.rv_selected_members);
        btnCreateProject = findViewById(R.id.btn_create_project);
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupListeners() {
        // Back button - finish activity
        btnBack.setOnClickListener(v -> onBackPressed());

        // Select member layout - open member selection dialog
        layoutSelectMember.setOnClickListener(v -> openMemberSelectionDialog());

        // Create project button - validate and save project
        btnCreateProject.setOnClickListener(v -> validateAndCreateProject());
    }

    /**
     * Setup RecyclerView for displaying selected members
     */
    private void setupSelectedMembersRecyclerView() {
        selectedMembersAdapter = new MemberAdapter();
        selectedMembersAdapter.setDisplayMode(true); // Display mode without interaction

        rvSelectedMembers.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedMembers.setAdapter(selectedMembersAdapter);
    }

    /**
     * Open the member selection bottom sheet dialog
     */
    private void openMemberSelectionDialog() {
        AddMemberBottomSheet bottomSheet = AddMemberBottomSheet.newInstance(selectedMembers);
        bottomSheet.setOnMembersSelectedListener(this);
        bottomSheet.show(getSupportFragmentManager(), "AddMemberBottomSheet");
    }

    /**
     * Callback when members are selected from the dialog
     */
    @Override
    public void onMembersSelected(List<Member> members) {
        // Update selected members list
        selectedMembers.clear();
        selectedMembers.addAll(members);

        // Update UI
        updateSelectedMembersUI();
    }

    /**
     * Update the UI to reflect selected members
     */
    private void updateSelectedMembersUI() {
        int memberCount = selectedMembers.size();

        if (memberCount > 0) {
            // Update text to show count
            tvSelectedMembers.setText(getString(R.string.members_selected, memberCount));
            tvSelectedMembers.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));

            // Show selected members in RecyclerView
            selectedMembersAdapter.setMembers(selectedMembers);
            rvSelectedMembers.setVisibility(View.VISIBLE);
        } else {
            // Reset to default state
            tvSelectedMembers.setText(R.string.select_member);
            tvSelectedMembers.setTextColor(getResources().getColor(R.color.text_hint, getTheme()));
            rvSelectedMembers.setVisibility(View.GONE);
        }
    }

    /**
     * Validate input and create the project
     */
    private void validateAndCreateProject() {
        // Get input values
        String title = etProjectTitle.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();

        // Validate project title
        if (TextUtils.isEmpty(title)) {
            etProjectTitle.setError(getString(R.string.error_project_title_required));
            etProjectTitle.requestFocus();
            Toast.makeText(this, R.string.error_project_title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent multiple submissions
        if (isCreating) {
            return;
        }

        isCreating = true;
        btnCreateProject.setEnabled(false);

        // Get owner ID from shared preferences
        long ownerId = prefsManager.getUserId();

        // Build create project request
        CreateProjectRequest request = new CreateProjectRequest(title, description, ownerId);

        // Make API call to create project
        Call<ProjectResponse> call = apiService.createProject(request);
        call.enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProjectResponse projectResponse = response.body();
                    Long projectId = projectResponse.getId();

                    // Add selected members to the project
                    if (selectedMembers.isEmpty()) {
                        // No members to add, project created successfully
                        Toast.makeText(CreateProjectActivity.this,
                                R.string.project_created_successfully, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // Add members to the project
                        addMembersToProject(projectId, 0);
                    }
                } else {
                    isCreating = false;
                    btnCreateProject.setEnabled(true);
                    handleCreateProjectError(response);
                }
            }

            @Override
            public void onFailure(Call<ProjectResponse> call, Throwable t) {
                isCreating = false;
                btnCreateProject.setEnabled(true);
                Toast.makeText(CreateProjectActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Recursively add selected members to the project
     */
    private void addMembersToProject(Long projectId, int memberIndex) {
        if (memberIndex >= selectedMembers.size()) {
            // All members added successfully
            Toast.makeText(this, R.string.project_created_successfully, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        Member member = selectedMembers.get(memberIndex);
        Long memberId = (long) member.getId();

        // Create add member request
        com.example.ProjectManager.models.dto.AddMemberRequest addMemberRequest = new com.example.ProjectManager.models.dto.AddMemberRequest(
                memberId);

        // Make API call to add member
        Call<Void> call = apiService.addMemberToProject(projectId, addMemberRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Add next member
                    addMembersToProject(projectId, memberIndex + 1);
                } else {
                    isCreating = false;
                    btnCreateProject.setEnabled(true);
                    Toast.makeText(CreateProjectActivity.this,
                            "Failed to add member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isCreating = false;
                btnCreateProject.setEnabled(true);
                Toast.makeText(CreateProjectActivity.this,
                        "Network error while adding member: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handle errors from create project API call
     */
    private void handleCreateProjectError(Response<ProjectResponse> response) {
        String errorMessage = "Error creating project";
        if (response.code() == 400) {
            errorMessage = "Invalid project data";
        } else if (response.code() == 401) {
            errorMessage = "Unauthorized - please login again";
            prefsManager.clearUserData();
            startActivity(new android.content.Intent(this, OnboardingActivity.class));
            finish();
            return;
        } else if (response.code() == 500) {
            errorMessage = "Server error - please try again";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        String title = etProjectTitle.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(description) || !selectedMembers.isEmpty()) {
            // Show confirmation dialog for unsaved changes
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Show dialog to confirm discarding unsaved changes
     */
    private void showUnsavedChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to go back?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
