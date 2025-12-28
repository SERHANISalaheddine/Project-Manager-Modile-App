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
import com.example.ProjectManager.database.ProjectDatabaseHelper;
import com.example.ProjectManager.fragments.AddMemberBottomSheet;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.Project;

import java.util.ArrayList;
import java.util.List;

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
    private ProjectDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        // Initialize data
        selectedMembers = new ArrayList<>();

        // Initialize database helper
        databaseHelper = new ProjectDatabaseHelper(this);

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

        // Create project object
        Project project = new Project(title, description, new ArrayList<>(selectedMembers));

        // Save to database
        boolean success = saveProject(project);

        if (success) {
            // Show success message
            Toast.makeText(this, R.string.project_created_successfully, Toast.LENGTH_SHORT).show();

            // Set result and finish
            setResult(RESULT_OK);
            finish();
        } else {
            // Show error message
            Toast.makeText(this, R.string.error_creating_project, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save the project to the database
     * 
     * @param project The project to save
     * @return true if successful, false otherwise
     */
    private boolean saveProject(Project project) {
        try {
            // TODO(API): Replace this local save with a call to
            // ApiService.createProject(CreateProjectRequest) via RetrofitClient
            // and handle the ProjectResponse. Keep local persistence as cache.
            // For now we persist locally so app works offline/standalone.
            // Save project using database helper (local mock persistence)
            long projectId = databaseHelper.insertProject(project);

            if (projectId > 0) {
                project.setId((int) projectId);
                // Also stash last created project locally (mock) for quick access
                getSharedPreferences("mock_projects", MODE_PRIVATE)
                        .edit()
                        .putString("last_title", project.getTitle())
                        .putString("last_description", project.getDescription())
                        .putInt("last_member_count", project.getMemberCount())
                        .apply();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        // Close database connection
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
