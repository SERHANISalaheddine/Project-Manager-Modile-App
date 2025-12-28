package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.ProjectAdapter;
import com.example.ProjectManager.database.ProjectDatabaseHelper;
import com.example.ProjectManager.models.Project;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.util.List;

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
    private boolean isCreatedTabSelected = true;

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
     * Load projects from database
     */
    private void loadProjects() {
        List<Project> projects = databaseHelper.getAllProjects();
        projectAdapter.setProjects(projects);

        // Update badge count
        updateBadgeCounts(projects.size());

        // Show empty state if no projects
        if (projects.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvProjects.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Update the badge counts on tabs
     */
    private void updateBadgeCounts(int createdCount) {
        tvCreatedBadge.setText(String.valueOf(createdCount));

        if (createdCount > 0) {
            tvCreatedBadge.setVisibility(View.VISIBLE);
        } else {
            tvCreatedBadge.setVisibility(View.GONE);
        }
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

        // Load projects user is part of (not implemented - would require user
        // authentication)
        // For now, show empty state
        projectAdapter.setProjects(null);
        rvProjects.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
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
