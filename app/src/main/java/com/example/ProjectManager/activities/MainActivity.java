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
    private SharedPrefsManager prefsManager;
    private long userId;

    // Project name cache for tasks
    private Map<Long, String> projectNameCache = new HashMap<>();

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
                    loadDashboardData();
                }
            });

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadDashboardData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        prefsManager = SharedPrefsManager.getInstance(this);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        userId = prefsManager.getUserId();

        // Check login
        if (userId <= 0) {
            navigateToLogin();
            return;
        }

        initViews();
        setupAdapters();
        setupListeners();
        updateGreeting();
        loadUserProfile();
        loadDashboardData();
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

        // Update navigation
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "home");
        NavigationUtils.setupNavigationListeners(this, navHome, navProjects, navTasks, navProfile);
    }

    private void setupAdapters() {
        // Projects adapter
        projectAdapter = new DashboardProjectAdapter();
        projectAdapter.setOnProjectClickListener(project -> {
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, project.getId());
            startActivity(intent);
        });
        rvRecentProjects.setLayoutManager(new LinearLayoutManager(this));
        rvRecentProjects.setAdapter(projectAdapter);

        // Tasks adapter
        taskAdapter = new DashboardTaskAdapter();
        taskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("projectId", task.getProjectId());
            startActivity(intent);
        });
        rvUpcomingTasks.setLayoutManager(new LinearLayoutManager(this));
        rvUpcomingTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        // Profile click
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // New Project button
        btnNewProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateProjectActivity.class);
            createProjectLauncher.launch(intent);
        });

        // New Task button
        btnNewTask.setOnClickListener(v -> {
            if (ownedProjectsCount + memberProjectsCount > 0) {
                Intent intent = new Intent(this, CreateTaskActivity.class);
                createTaskLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Create a project first!", Toast.LENGTH_SHORT).show();
            }
        });

        // View all projects
        btnViewAllProjects.setOnClickListener(v -> {
            startActivity(new Intent(this, ProjectsActivity.class));
        });

        // View all tasks
        btnViewAllTasks.setOnClickListener(v -> {
            startActivity(new Intent(this, MyTasksActivity.class));
        });
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 18) {
            greeting = "Good Afternoon";
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
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    tvUserName.setText("Welcome, " + user.getFirstName() + "!");

                    // Use ImageUtils to properly convert profile picture URL
                    String profilePicUrl = com.example.ProjectManager.utils.ImageUtils.getProfilePictureUrl(user.getProfilePictureUrl());
                    if (profilePicUrl != null) {
                        Glide.with(MainActivity.this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(ivProfile);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
            }
        });
    }

    private void loadDashboardData() {
        ownedProjectsCount = 0;
        memberProjectsCount = 0;
        projectNameCache.clear();
        loadOwnedProjects();
    }

    private void loadOwnedProjects() {
        apiService.getProjectsByOwner(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call, Response<PageResponse<ProjectResponse>> response) {
                List<DashboardProjectAdapter.ProjectItem> allProjects = new ArrayList<>();
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> owned = response.body().getContent();
                    ownedProjectsCount = owned.size();
                    
                    for (ProjectResponse p : owned) {
                        allProjects.add(new DashboardProjectAdapter.ProjectItem(
                                p.getId(), p.getName(), p.getDescription(), true));
                        projectNameCache.put(p.getId(), p.getName());
                    }
                }
                
                loadMemberProjects(allProjects);
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                loadMemberProjects(new ArrayList<>());
            }
        });
    }

    private void loadMemberProjects(List<DashboardProjectAdapter.ProjectItem> allProjects) {
        apiService.getProjectsByMember(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call, Response<PageResponse<ProjectResponse>> response) {
                Set<Long> existingIds = new HashSet<>();
                for (DashboardProjectAdapter.ProjectItem p : allProjects) {
                    existingIds.add(p.getId());
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> member = response.body().getContent();
                    
                    for (ProjectResponse p : member) {
                        if (!existingIds.contains(p.getId())) {
                            allProjects.add(new DashboardProjectAdapter.ProjectItem(
                                    p.getId(), p.getName(), p.getDescription(), false));
                            existingIds.add(p.getId());
                            projectNameCache.put(p.getId(), p.getName());
                        }
                    }
                    memberProjectsCount = member.size();
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

    private void updateProjectsUI(List<DashboardProjectAdapter.ProjectItem> allProjects) {
        int totalProjects = allProjects.size();
        tvTotalProjects.setText(String.valueOf(totalProjects));
        tvProjectDetails.setText(ownedProjectsCount + " owned - " + memberProjectsCount + " member");

        List<DashboardProjectAdapter.ProjectItem> recentProjects = allProjects.size() > 5 
                ? allProjects.subList(0, 5) : allProjects;
        
        projectAdapter.setProjects(recentProjects);

        if (allProjects.isEmpty()) {
            rvRecentProjects.setVisibility(View.GONE);
            tvEmptyProjects.setVisibility(View.VISIBLE);
        } else {
            rvRecentProjects.setVisibility(View.VISIBLE);
            tvEmptyProjects.setVisibility(View.GONE);
        }
    }

    private void loadTasks() {
        apiService.getAllTasks(0, 100, userId, null, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<TaskResponse>> call, Response<PageResponse<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> tasks = response.body().getContent();
                    processTasks(tasks);
                } else {
                    updateTasksUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<TaskResponse>> call, Throwable t) {
                updateTasksUI(new ArrayList<>());
            }
        });
    }

    private void processTasks(List<TaskResponse> tasks) {
        totalTasksCount = tasks.size();
        todoCount = 0;
        inProgressCount = 0;
        doneCount = 0;

        List<DashboardTaskAdapter.TaskItem> upcomingTasks = new ArrayList<>();

        for (TaskResponse task : tasks) {
            String status = task.getStatus();
            
            switch (status) {
                case "TODO":
                    todoCount++;
                    break;
                case "IN_PROGRESS":
                    inProgressCount++;
                    break;
                case "DONE":
                    doneCount++;
                    break;
            }

            if (!"DONE".equals(status) && !"ARCHIVED".equals(status)) {
                String projectName = projectNameCache.get(task.getProjectId());
                upcomingTasks.add(new DashboardTaskAdapter.TaskItem(
                        task.getId(),
                        task.getName(),
                        status,
                        task.getProjectId(),
                        projectName != null ? projectName : "Project #" + task.getProjectId()
                ));
            }
        }

        upcomingTasks.sort((a, b) -> {
            if ("IN_PROGRESS".equals(a.getStatus()) && !"IN_PROGRESS".equals(b.getStatus())) {
                return -1;
            } else if (!"IN_PROGRESS".equals(a.getStatus()) && "IN_PROGRESS".equals(b.getStatus())) {
                return 1;
            }
            return 0;
        });

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

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId > 0) {
            loadDashboardData();
        }
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "home");
    }
}
