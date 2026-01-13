package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.TaskCardAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.utils.NavigationUtils;
import com.example.ProjectManager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTasksActivity extends AppCompatActivity {

    private static final String TAG = "MyTasksActivity";

    // UI Components
    private EditText etSearch;
    private TabLayout tabFilter;
    private TextView tvTotalCount, tvTodoCount, tvInProgressCount, tvDoneCount;
    private RecyclerView rvTasks;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;

    // Bottom Navigation
    private LinearLayout navHome, navProjects, navTasks, navProfile;

    // Data
    private ApiService apiService;
    private SessionManager sessionManager;
    private TaskCardAdapter adapter;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private List<TaskResponse> displayedTasks = new ArrayList<>();

    private String currentFilter = "all"; // all, todo, in_progress, done
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);

        initViews();
        setupServices();
        setupRecyclerView();
        setupTabFilter();
        setupSearch();
        setupNavigation();

        loadTasks();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        tabFilter = findViewById(R.id.tab_filter);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvTodoCount = findViewById(R.id.tv_todo_count);
        tvInProgressCount = findViewById(R.id.tv_in_progress_count);
        tvDoneCount = findViewById(R.id.tv_done_count);
        rvTasks = findViewById(R.id.rv_tasks);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);

        navHome = findViewById(R.id.nav_home);
        navProjects = findViewById(R.id.nav_projects);
        navTasks = findViewById(R.id.nav_tasks);
        navProfile = findViewById(R.id.nav_profile);

        // Setup swipe refresh (only if exists)
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(
                    getResources().getColor(R.color.primary, null),
                    getResources().getColor(R.color.status_in_progress, null),
                    getResources().getColor(R.color.status_done, null)
            );
            swipeRefresh.setOnRefreshListener(this::loadTasks);
        }
    }

    private void setupServices() {
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
    }

    private void setupRecyclerView() {
        adapter = new TaskCardAdapter(displayedTasks, task -> {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        });

        if (rvTasks != null) {
            rvTasks.setLayoutManager(new LinearLayoutManager(this));
            rvTasks.setAdapter(adapter);
        }
    }

    private void setupTabFilter() {
        if (tabFilter == null) return;
        
        tabFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "all";
                        break;
                    case 1:
                        currentFilter = "todo";
                        break;
                    case 2:
                        currentFilter = "in_progress";
                        break;
                    case 3:
                        currentFilter = "done";
                        break;
                }
                filterAndDisplayTasks();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        if (etSearch == null) return;
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                filterAndDisplayTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupNavigation() {
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "tasks");
        NavigationUtils.setupNavigationListeners(this, navHome, navProjects, navTasks, navProfile);
    }

    private void loadTasks() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        // Load all tasks for user
        apiService.getAllTasks(0, 100, userId, null, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call, 
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    allTasks.clear();
                    allTasks.addAll(response.body().getContent());
                    Log.d(TAG, "Loaded " + allTasks.size() + " tasks");
                    
                    updateStats();
                    filterAndDisplayTasks();
                } else {
                    Log.e(TAG, "Failed to load tasks: " + response.code());
                    Toast.makeText(MyTasksActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                Log.e(TAG, "Error loading tasks", t);
                Toast.makeText(MyTasksActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateStats() {
        int totalCount = allTasks.size();
        int todoCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;

        for (TaskResponse task : allTasks) {
            String status = task.getStatus();
            if (status != null) {
                switch (status.toUpperCase()) {
                    case "TODO":
                    case "TO_DO":
                    case "TO DO":
                        todoCount++;
                        break;
                    case "IN_PROGRESS":
                    case "IN PROGRESS":
                        inProgressCount++;
                        break;
                    case "DONE":
                    case "COMPLETED":
                        doneCount++;
                        break;
                }
            }
        }

        if (tvTotalCount != null) {
            tvTotalCount.setText(String.valueOf(totalCount));
        }
        if (tvTodoCount != null) {
            tvTodoCount.setText(String.valueOf(todoCount));
        }
        if (tvInProgressCount != null) {
            tvInProgressCount.setText(String.valueOf(inProgressCount));
        }
        if (tvDoneCount != null) {
            tvDoneCount.setText(String.valueOf(doneCount));
        }
    }

    private void filterAndDisplayTasks() {
        displayedTasks.clear();

        for (TaskResponse task : allTasks) {
            // Apply status filter
            if (!matchesStatusFilter(task)) {
                continue;
            }

            // Apply search filter
            if (!searchQuery.isEmpty() && !matchesSearch(task)) {
                continue;
            }

            displayedTasks.add(task);
        }

        // Update UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private boolean matchesStatusFilter(TaskResponse task) {
        if (currentFilter.equals("all")) {
            return true;
        }

        String status = task.getStatus();
        if (status == null) return false;

        switch (currentFilter) {
            case "todo":
                return status.equalsIgnoreCase("TODO") || 
                       status.equalsIgnoreCase("TO_DO") ||
                       status.equalsIgnoreCase("TO DO");
            case "in_progress":
                return status.equalsIgnoreCase("IN_PROGRESS") || 
                       status.equalsIgnoreCase("IN PROGRESS");
            case "done":
                return status.equalsIgnoreCase("DONE") || 
                       status.equalsIgnoreCase("COMPLETED");
            default:
                return true;
        }
    }

    private boolean matchesSearch(TaskResponse task) {
        if (task.getTitle() != null && task.getTitle().toLowerCase().contains(searchQuery)) {
            return true;
        }
        if (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchQuery)) {
            return true;
        }
        return false;
    }

    private void updateEmptyState() {
        if (emptyState == null || rvTasks == null) return;
        
        if (displayedTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
}
