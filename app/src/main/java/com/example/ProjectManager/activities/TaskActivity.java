package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.TaskAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.utils.NavigationUtils;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ApiService apiService;

    // UI Elements
    private ExtendedFloatingActionButton createTaskBtn;
    private LinearLayout navHome, navProjects, navTasks, navProfile;
    private LinearLayout emptyState;
    private TextView tvProjectName, tvSectionTitle, tvTaskCount;
    private TextView tvTodoCount, tvInProgressCount, tvDoneCount;

    // Filter Chips
    private ChipGroup chipGroup;
    private Chip chipAll, chipTodo, chipInProgress, chipDone, chipArchived;

    // Project filter
    private long projectId = -1;
    private String projectName = null;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Initialize SharedPrefs Manager
        prefsManager = SharedPrefsManager.getInstance(this);

        // Initialize views
        initViews();

        // Initialize adapter
        adapter = new TaskAdapter();
        adapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(TaskActivity.this, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // API
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // Get projectId from Intent
        if (getIntent() != null) {
            projectId = getIntent().getLongExtra("projectId", -1);
            projectName = getIntent().getStringExtra("projectName");

            if (projectName != null && tvProjectName != null) {
                tvProjectName.setText(projectName);
            } else {
                tvProjectName.setText("All Projects");
            }

            // Save this project as the last opened one
            if (projectId > 0) {
                prefsManager.saveLastProjectId(projectId);
                if (projectName != null) {
                    prefsManager.saveLastProjectName(projectName);
                }
            }
        }

        // Initialize navigation
        initializeNavigation();

        // Setup filter chips
        setupFilterChips();

        // Create Task Button
        createTaskBtn.setOnClickListener(v -> {
            Intent i = new Intent(TaskActivity.this, CreateTaskActivity.class);
            if (projectId > 0) {
                i.putExtra("projectId", projectId);
                i.putExtra("projectName", projectName);
            } else {
                // If no project selected, try to use last project
                long lastProjectId = prefsManager.getLastProjectId();
                String lastProjectName = prefsManager.getLastProjectName();
                if (lastProjectId > 0) {
                    i.putExtra("projectId", lastProjectId);
                    i.putExtra("projectName", lastProjectName);
                }
            }
            startActivity(i);
        });

        // First load
        loadTasksFromBackend();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.taskRecyclerView);
        createTaskBtn = findViewById(R.id.createTaskBtn);
        emptyState = findViewById(R.id.emptyState);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvTaskCount = findViewById(R.id.tvTaskCount);

        // Stats counters
        tvTodoCount = findViewById(R.id.tvTodoCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvDoneCount = findViewById(R.id.tvDoneCount);

        // Chips
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipTodo = findViewById(R.id.chipTodo);
        chipInProgress = findViewById(R.id.chipInProgress);
        chipDone = findViewById(R.id.chipDone);
        chipArchived = findViewById(R.id.chipArchived);
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                adapter.filterByStatus(null);
                tvSectionTitle.setText("All Tasks");
            } else if (checkedId == R.id.chipTodo) {
                adapter.filterByStatus("TODO");
                tvSectionTitle.setText("To Do");
            } else if (checkedId == R.id.chipInProgress) {
                adapter.filterByStatus("IN_PROGRESS");
                tvSectionTitle.setText("In Progress");
            } else if (checkedId == R.id.chipDone) {
                adapter.filterByStatus("DONE");
                tvSectionTitle.setText("Done");
            } else if (checkedId == R.id.chipArchived) {
                adapter.filterByStatus("ARCHIVED");
                tvSectionTitle.setText("Archived");
            }
            
            updateTaskCount();
            updateEmptyState();
        });
    }

    private void initializeNavigation() {
        navHome = findViewById(R.id.nav_home);
        navProjects = findViewById(R.id.nav_projects);
        navTasks = findViewById(R.id.nav_tasks);
        navProfile = findViewById(R.id.nav_profile);

        // Setup navigation listeners
        NavigationUtils.setupNavigationListeners(this, navHome, navProjects, navTasks, navProfile, "tasks");

        // Update navigation to highlight tasks icon
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "tasks");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when coming back from CreateTaskActivity
        loadTasksFromBackend();
        // Update navigation again to ensure correct highlighting
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "tasks");
    }

    private void loadTasksFromBackend() {
        Call<PageResponse<TaskResponse>> call;

        if (projectId > 0) {
            // Filter tasks by project
            call = apiService.getAllTasks(0, 100, null, projectId, null);
        } else {
            // Load all tasks (no project filter)
            call = apiService.getAllTasks(0, 100, null, null, null);
        }

        call.enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<TaskResponse>> call,
                    Response<PageResponse<TaskResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getContent() != null) {
                        adapter.setItems(response.body().getContent());
                    } else {
                        adapter.setItems(null);
                    }
                    updateStats();
                    updateTaskCount();
                    updateEmptyState();
                } else {
                    Toast.makeText(TaskActivity.this,
                            "Error loading tasks: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<TaskResponse>> call, Throwable t) {
                Toast.makeText(TaskActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats() {
        if (tvTodoCount != null) {
            tvTodoCount.setText(String.valueOf(adapter.getCountByStatus("TODO")));
        }
        if (tvInProgressCount != null) {
            tvInProgressCount.setText(String.valueOf(adapter.getCountByStatus("IN_PROGRESS")));
        }
        if (tvDoneCount != null) {
            tvDoneCount.setText(String.valueOf(adapter.getCountByStatus("DONE")));
        }
    }

    private void updateTaskCount() {
        int count = adapter.getItemCount();
        tvTaskCount.setText(count + (count == 1 ? " task" : " tasks"));
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
