package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.ProjectAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.database.ProjectDatabaseHelper;
import com.example.ProjectManager.models.Project;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main activity serving as the home dashboard for the app.
 * Displays a list of projects and provides navigation to create new projects.
 */
public class MainActivity extends AppCompatActivity implements ProjectAdapter.OnProjectClickListener {

    // UI Components
    private LinearLayout tabCreated;
    private LinearLayout tabPartOf;
    private TextView tvTabCreated;
    private TextView tvTabPartOf;
    private TextView tvCreatedBadge;
    private TextView tvPartOfBadge;
    private RecyclerView rvProjects;
    private LinearLayout layoutEmptyState;
    private Button btnCreateProject;

    // Navigation
    private LinearLayout navHome;
    private LinearLayout navCalendar;
    private LinearLayout navTasks;
    private LinearLayout navProfile;

    // Data
    private ProjectAdapter projectAdapter;
    private ProjectDatabaseHelper databaseHelper;
    private SharedPrefsManager prefsManager;
    private ApiService apiService;
    private boolean isCreatedTabSelected = true;
    private int createdProjectsCount = 0;
    private int partOfProjectsCount = 0;

    // Activity Result Launcher for Create Project
    private final ActivityResultLauncher<Intent> createProjectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh project list when a new project is created
                    loadProjects();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new ProjectDatabaseHelper(this);

        // Initialize shared preferences manager
        prefsManager = SharedPrefsManager.getInstance(this);

        // Initialize API service
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();

        // Setup RecyclerView
        setupRecyclerView();

        // Load projects
        loadProjects();
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        // Tabs
        tabCreated = findViewById(R.id.tab_created);
        tabPartOf = findViewById(R.id.tab_part_of);
        tvTabCreated = findViewById(R.id.tv_tab_created);
        tvTabPartOf = findViewById(R.id.tv_tab_part_of);
        tvCreatedBadge = findViewById(R.id.tv_created_badge);
        tvPartOfBadge = findViewById(R.id.tv_part_of_badge);

        // Content
        rvProjects = findViewById(R.id.rv_projects);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnCreateProject = findViewById(R.id.btn_create_project);

        // Bottom Navigation
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navTasks = findViewById(R.id.nav_tasks);
        navProfile = findViewById(R.id.nav_profile);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Tab listeners
        tabCreated.setOnClickListener(v -> selectCreatedTab());
        tabPartOf.setOnClickListener(v -> selectPartOfTab());

        // Create project button
        btnCreateProject.setOnClickListener(v -> openCreateProjectScreen());

        // Bottom navigation listeners
        navHome.setOnClickListener(v -> {
            // Already on home, do nothing or refresh
            loadProjects();
        });

        navCalendar.setOnClickListener(v -> {
            // Navigate to calendar (not implemented)
            showFeatureNotAvailable("Calendar");
        });

        navTasks.setOnClickListener(v -> {
            // Navigate to tasks (not implemented)
            showFeatureNotAvailable("Tasks");
        });

        navProfile.setOnClickListener(v -> {
            // Navigate to profile (not implemented)
            showFeatureNotAvailable("Profile");
        });
    }

    /**
     * Setup the RecyclerView with adapter
     */
    private void setupRecyclerView() {
        projectAdapter = new ProjectAdapter();
        projectAdapter.setOnProjectClickListener(this);

        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(projectAdapter);
    }

    /**
     * Load projects from API based on selected tab
     */
    private void loadProjects() {
        long userId = prefsManager.getUserId();
        if (userId <= 0) {
            // User not logged in properly, show empty state
            showEmptyState();
            return;
        }

        if (isCreatedTabSelected) {
            loadCreatedProjects(userId);
        } else {
            loadMemberProjects(userId);
        }
    }

    /**
     * Load projects owned by the user
     * GET /api/v1/projects/owner/{userId}
     */
    private void loadCreatedProjects(long userId) {
        Call<PageResponse<ProjectResponse>> call = apiService.getProjectsByOwner(userId, 0, 50);
        call.enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call,
                    Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<ProjectResponse> pageResponse = response.body();
                    List<ProjectResponse> projectResponses = pageResponse.getContent();

                    // Convert to Project model
                    List<Project> projects = convertToProjects(projectResponses);
                    createdProjectsCount = projects.size();

                    projectAdapter.setProjects(projects);
                    updateBadgeCounts();

                    if (projects.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "Failed to load projects: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                // Fallback to local database
                loadProjectsFromDatabase();
            }
        });
    }

    /**
     * Load projects where the user is a member
     * GET /api/v1/projects/member/{userId}
     */
    private void loadMemberProjects(long userId) {
        Call<PageResponse<ProjectResponse>> call = apiService.getProjectsByMember(userId, 0, 50);
        call.enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call,
                    Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<ProjectResponse> pageResponse = response.body();
                    List<ProjectResponse> projectResponses = pageResponse.getContent();

                    // Convert to Project model
                    List<Project> projects = convertToProjects(projectResponses);
                    partOfProjectsCount = projects.size();

                    projectAdapter.setProjects(projects);
                    updateBadgeCounts();

                    if (projects.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "Failed to load projects: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    /**
     * Convert API ProjectResponse to local Project model
     */
    private List<Project> convertToProjects(List<ProjectResponse> projectResponses) {
        List<Project> projects = new ArrayList<>();
        if (projectResponses != null) {
            for (ProjectResponse pr : projectResponses) {
                Project project = new Project(
                        pr.getId(),
                        pr.getName(),
                        pr.getDescription());
                projects.add(project);
            }
        }
        return projects;
    }

    /**
     * Fallback: Load projects from local database
     */
    private void loadProjectsFromDatabase() {
        List<Project> projects = databaseHelper.getAllProjects();
        projectAdapter.setProjects(projects);
        createdProjectsCount = projects.size();
        updateBadgeCounts();

        if (projects.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        rvProjects.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        rvProjects.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    /**
     * Update the badge counts on tabs
     */
    private void updateBadgeCounts() {
        // Created tab badge
        tvCreatedBadge.setText(String.valueOf(createdProjectsCount));
        tvCreatedBadge.setVisibility(createdProjectsCount > 0 ? View.VISIBLE : View.GONE);

        // Part Of tab badge
        tvPartOfBadge.setText(String.valueOf(partOfProjectsCount));
        tvPartOfBadge.setVisibility(partOfProjectsCount > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Select the "Created" tab
     */
    private void selectCreatedTab() {
        isCreatedTabSelected = true;

        // Update tab appearance
        tabCreated.setSelected(true);
        tabPartOf.setSelected(false);

        tvTabCreated.setTextColor(getResources().getColor(R.color.purple_primary, getTheme()));
        tvTabPartOf.setTextColor(getResources().getColor(R.color.white, getTheme()));

        // Load created projects
        loadProjects();
    }

    /**
     * Select the "Part Of" tab
     */
    private void selectPartOfTab() {
        isCreatedTabSelected = false;

        // Update tab appearance
        tabCreated.setSelected(false);
        tabPartOf.setSelected(true);

        tvTabCreated.setTextColor(getResources().getColor(R.color.white, getTheme()));
        tvTabPartOf.setTextColor(getResources().getColor(R.color.purple_primary, getTheme()));

        // Load projects user is a member of from API
        loadProjects();
    }

    /**
     * Open the create project screen
     */
    private void openCreateProjectScreen() {
        Intent intent = new Intent(this, CreateProjectActivity.class);
        createProjectLauncher.launch(intent);
    }

    /**
     * Show a toast for features not yet implemented
     */
    private void showFeatureNotAvailable(String feature) {
        android.widget.Toast.makeText(this,
                feature + " feature coming soon!",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    // ProjectAdapter.OnProjectClickListener implementation

    @Override
    public void onProjectClick(Project project, int position) {
        // Open project details (not implemented)
        android.widget.Toast.makeText(this,
                "Project: " + project.getTitle(),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProjectLongClick(Project project, int position) {
        // Show delete confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete \"" + project.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteProject(project.getId());
                    projectAdapter.removeProject(position);
                    loadProjects(); // Refresh to update empty state and badges
                    android.widget.Toast.makeText(this,
                            "Project deleted",
                            android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh projects when returning to this screen
        if (isCreatedTabSelected) {
            loadProjects();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle user logout
     */
    private void handleLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear user data
                    prefsManager.clearUserData();

                    // Show success message
                    android.widget.Toast.makeText(this,
                            R.string.logout_successful,
                            android.widget.Toast.LENGTH_SHORT).show();

                    // Navigate to onboarding screen
                    Intent intent = new Intent(this, OnboardingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
