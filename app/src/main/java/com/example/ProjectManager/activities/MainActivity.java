package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.DashboardProjectAdapter;
import com.example.ProjectManager.adapters.DashboardTaskAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.NavigationUtils;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.example.ProjectManager.utils.NavigationUtils;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main Dashboard Activity - Matches web app design.
 * Shows greeting, stats cards, progress overview, recent projects, and upcoming tasks.
 */
public class MainActivity extends AppCompatActivity {

    // Header Views
    private TextView tvGreeting;
    private TextView tvUserName;
    private CircleImageView ivProfile;
    private MaterialButton btnNewProject;
    private MaterialButton btnNewTask;

    // Stats Cards
    private TextView tvTotalProjects;
    private TextView tvProjectDetails;
    private TextView tvTotalTasks;
    private TextView tvTaskDetails;
    private TextView tvInProgress;
    private TextView tvCompleted;
    private TextView tvCompletedDetails;

    // Progress Overview
    private TextView tvProgressPercent;
    private ProgressBar progressBar;
    private TextView tvTodoCount;
    private TextView tvActiveCount;
    private TextView tvDoneCount;

    // RecyclerViews
    private RecyclerView rvRecentProjects;
    private RecyclerView rvUpcomingTasks;
    private TextView tvEmptyProjects;
    private TextView tvEmptyTasks;
    private TextView btnViewAllProjects;
    private TextView btnViewAllTasks;

    // Bottom Navigation
    private LinearLayout navHome;
    private LinearLayout navProjects;
    private LinearLayout navTasks;
    private LinearLayout navProfile;

    // Adapters
    private DashboardProjectAdapter projectAdapter;
    private DashboardTaskAdapter taskAdapter;

    // Data
    private ApiService apiService;

    // Tab state
    private boolean isCreatedTabSelected = true;
    private int createdProjectsCount = 0;
    private int partOfProjectsCount = 0;

    // Stats
    private int ownedProjectsCount = 0;
    private int memberProjectsCount = 0;
    private int totalTasksCount = 0;
    private int todoCount = 0;
    private int inProgressCount = 0;
    private int doneCount = 0;

    // Activity launchers
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
        userId = prefsManager.getUserId();

        initViews();
        setupListeners();
        setupRecyclerView();

        // Highlight home icon
        NavigationUtils.updateNavigation(navHome, navCalendar, navTasks, navProfile, "home");

        // IMPORTANT: make the UI match the default tab state
        updateTabsUI();

        loadProjects();
    }

    private void initViews() {
        // Header
        tvGreeting = findViewById(R.id.tv_greeting);
        tvUserName = findViewById(R.id.tv_user_name);
        ivProfile = findViewById(R.id.iv_profile);
        btnNewProject = findViewById(R.id.btn_new_project);
        btnNewTask = findViewById(R.id.btn_new_task);

        // Stats Cards
        tvTotalProjects = findViewById(R.id.tv_total_projects);
        tvProjectDetails = findViewById(R.id.tv_project_details);
        tvTotalTasks = findViewById(R.id.tv_total_tasks);
        tvTaskDetails = findViewById(R.id.tv_task_details);
        tvInProgress = findViewById(R.id.tv_in_progress);
        tvCompleted = findViewById(R.id.tv_completed);
        tvCompletedDetails = findViewById(R.id.tv_completed_details);

        // Progress Overview
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        progressBar = findViewById(R.id.progress_bar);
        tvTodoCount = findViewById(R.id.tv_todo_count);
        tvActiveCount = findViewById(R.id.tv_active_count);
        tvDoneCount = findViewById(R.id.tv_done_count);

        // RecyclerViews
        rvRecentProjects = findViewById(R.id.rv_recent_projects);
        rvUpcomingTasks = findViewById(R.id.rv_upcoming_tasks);
        tvEmptyProjects = findViewById(R.id.tv_empty_projects);
        tvEmptyTasks = findViewById(R.id.tv_empty_tasks);
        btnViewAllProjects = findViewById(R.id.btn_view_all_projects);
        btnViewAllTasks = findViewById(R.id.btn_view_all_tasks);

        // Bottom Navigation
        navHome = findViewById(R.id.nav_home);
        navProjects = findViewById(R.id.nav_projects);
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

        // Update navigation
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "home");
        NavigationUtils.setupNavigationListeners(this, navHome, navProjects, navTasks, navProfile);
    }

        // Bottom navigation listeners
        navHome.setOnClickListener(v -> loadProjects());

        navCalendar.setOnClickListener(v -> showFeatureNotAvailable("Calendar"));

        navTasks.setOnClickListener(v -> openTasksForLastProject());

        navProfile.setOnClickListener(v -> showFeatureNotAvailable("Profile"));
    }

        // View all projects
        btnViewAllProjects.setOnClickListener(v -> {
            startActivity(new Intent(this, ProjectsActivity.class));
        });

        // View all tasks
        btnViewAllTasks.setOnClickListener(v -> {
            startActivity(new Intent(this, MyTasksActivity.class));
        });
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
            greeting = "Good Evening";
        }
        tvGreeting.setText(greeting);
    }

    private void loadUserProfile() {
        String firstName = prefsManager.getUserFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            tvUserName.setText("Welcome, " + firstName + "!");
        }

        apiService.getUser(userId).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call,
                                   Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    tvUserName.setText("Welcome, " + user.getFirstName() + "!");

                    String profilePicUrl = user.getProfilePictureUrl();
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        String fullUrl = com.example.ProjectManager.utils.Constants.BASE_URL + profilePicUrl;
                        Glide.with(MainActivity.this)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(ivProfile);
                    }
                }
            }

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
                
                loadMemberProjects(allProjects);
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

    private void loadMemberProjects(List<DashboardProjectAdapter.ProjectItem> allProjects) {
        apiService.getProjectsByMember(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
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
                
                updateProjectsUI(allProjects);
                loadTasks();
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                updateProjectsUI(allProjects);
                loadTasks();
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

        updateTasksUI(upcomingTasks);
    }

    private void updateTasksUI(List<DashboardTaskAdapter.TaskItem> upcomingTasks) {
        tvTotalTasks.setText(String.valueOf(totalTasksCount));
        tvInProgress.setText(String.valueOf(inProgressCount));
        tvCompleted.setText(String.valueOf(doneCount));
        
        tvTodoCount.setText(String.valueOf(todoCount));
        tvActiveCount.setText(String.valueOf(inProgressCount));
        tvDoneCount.setText(String.valueOf(doneCount));

        int progress = totalTasksCount > 0 ? (doneCount * 100 / totalTasksCount) : 0;
        tvProgressPercent.setText(progress + "%");
        progressBar.setProgress(progress);

        List<DashboardTaskAdapter.TaskItem> recentTasks = upcomingTasks.size() > 5 
                ? upcomingTasks.subList(0, 5) : upcomingTasks;
        
        taskAdapter.setTasks(recentTasks);

        if (upcomingTasks.isEmpty()) {
            rvUpcomingTasks.setVisibility(View.GONE);
            tvEmptyTasks.setVisibility(View.VISIBLE);
        } else {
            rvUpcomingTasks.setVisibility(View.VISIBLE);
            tvEmptyTasks.setVisibility(View.GONE);
        }
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
