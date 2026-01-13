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
import com.example.ProjectManager.utils.NavigationUtils;
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

    // Tab state
    private boolean isCreatedTabSelected = true;
    private int createdProjectsCount = 0;
    private int partOfProjectsCount = 0;

    // Activity Result Launcher for Create Project
    private final ActivityResultLauncher<Intent> createProjectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadProjects();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new ProjectDatabaseHelper(this);
        prefsManager = SharedPrefsManager.getInstance(this);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        initViews();
        setupListeners();
        setupRecyclerView();

        // Highlight home icon
        NavigationUtils.updateNavigation(navHome, navCalendar, navTasks, navProfile, "home");

        // IMPORTANT: make the UI match the default tab state
        updateTabsUI();

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

        // Tabs listeners
        tabCreated.setOnClickListener(v -> selectCreatedTab());
        tabPartOf.setOnClickListener(v -> selectPartOfTab());

        // Create project button
        btnCreateProject.setOnClickListener(v -> openCreateProjectScreen());

        // Bottom navigation listeners
        navHome.setOnClickListener(v -> loadProjects());

        navCalendar.setOnClickListener(v -> showFeatureNotAvailable("Calendar"));

        navTasks.setOnClickListener(v -> openTasksForLastProject());

        navProfile.setOnClickListener(v -> showFeatureNotAvailable("Profile"));
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

                    List<Project> projects = convertToProjects(projectResponses);
                    createdProjectsCount = projects.size();

                    projectAdapter.setProjects(projects);
                    updateBadgeCounts();
                    updateTabsUI();


                    if (projects.isEmpty()) showEmptyState();
                    else hideEmptyState();

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

                    List<Project> projects = convertToProjects(projectResponses);
                    partOfProjectsCount = projects.size();

                    projectAdapter.setProjects(projects);
                    updateBadgeCounts();
                    updateTabsUI();


                    if (projects.isEmpty()) showEmptyState();
                    else hideEmptyState();

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
                projects.add(new Project(pr.getId(), pr.getName(), pr.getDescription()));
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

        if (projects.isEmpty()) showEmptyState();
        else hideEmptyState();
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
        tvCreatedBadge.setText(String.valueOf(createdProjectsCount));
        tvCreatedBadge.setVisibility(createdProjectsCount > 0 ? View.VISIBLE : View.GONE);

        tvPartOfBadge.setText(String.valueOf(partOfProjectsCount));
        tvPartOfBadge.setVisibility(partOfProjectsCount > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Fix: Update tab UI (background + text colors) so toggling works.
     */
    private void updateTabsUI() {
        if (isCreatedTabSelected) {
            // Created selected
            tabCreated.setBackgroundResource(R.drawable.bg_pill_selected);
            tabPartOf.setBackgroundResource(android.R.color.transparent);

            tvTabCreated.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            tvTabPartOf.setTextColor(getResources().getColor(R.color.text_hint, getTheme()));

            // BADGES: selected red, unselected gray
            tvCreatedBadge.setBackgroundResource(R.drawable.bg_badge_red);
            tvCreatedBadge.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

            tvPartOfBadge.setBackgroundResource(R.drawable.bg_badge_gray);
            tvPartOfBadge.setTextColor(getResources().getColor(R.color.text_hint, getTheme()));

        } else {
            // Part Of selected
            tabPartOf.setBackgroundResource(R.drawable.bg_pill_selected);
            tabCreated.setBackgroundResource(android.R.color.transparent);

            tvTabPartOf.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            tvTabCreated.setTextColor(getResources().getColor(R.color.text_hint, getTheme()));

            // BADGES: selected red, unselected gray
            tvPartOfBadge.setBackgroundResource(R.drawable.bg_badge_red);
            tvPartOfBadge.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

            tvCreatedBadge.setBackgroundResource(R.drawable.bg_badge_gray);
            tvCreatedBadge.setTextColor(getResources().getColor(R.color.text_hint, getTheme()));
        }
    }


    /**
     * Select Created tab
     */
    private void selectCreatedTab() {
        isCreatedTabSelected = true;
        updateTabsUI();
        loadProjects();
    }

    /**
     * Select Part Of tab
     */
    private void selectPartOfTab() {
        isCreatedTabSelected = false;
        updateTabsUI();
        loadProjects();
    }

    /**
     * Open the create project screen
     */
    private void openCreateProjectScreen() {
        Intent intent = new Intent(this, CreateProjectActivity.class);
        createProjectLauncher.launch(intent);
    }

    private void showFeatureNotAvailable(String feature) {
        Toast.makeText(this, feature + " feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    // ProjectAdapter.OnProjectClickListener

    @Override
    public void onProjectClick(Project project, int position) {
        if (isCreatedTabSelected) {
            openTasksForProject(project);
        } else {
            Toast.makeText(this, "Only created projects can be opened", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProjectLongClick(Project project, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete \"" + project.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteProject(project.getId());
                    projectAdapter.removeProject(position);
                    loadProjects();
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
        NavigationUtils.updateNavigation(navHome, navCalendar, navTasks, navProfile, "home");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
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

    private void handleLogout() {
        prefsManager.clearUserData();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void openTasksForProject(Project project) {
        prefsManager.saveLastProjectId(project.getId());

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("projectId", (long) project.getId());
        intent.putExtra("projectName", project.getTitle());
        startActivity(intent);
    }

    private void openTasksForLastProject() {
        long lastProjectId = prefsManager.getLastProjectId();
        if (lastProjectId <= 0) {
            Toast.makeText(this, "Please select a project first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("projectId", lastProjectId);
        startActivity(intent);
    }
}
