package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.SearchResultAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ImageView btnBack, btnClear;
    private EditText editSearch;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipProjects, chipTasks, chipMembers;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerResults;
    private View emptyState, noResultsState, loadingOverlay;

    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private SearchResultAdapter adapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private String currentQuery = "";
    private String currentFilter = "all"; // all, projects, tasks, members

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupAdapter();
        setupListeners();
        setupBackPressHandler();

        // Focus search input
        editSearch.requestFocus();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnClear = findViewById(R.id.btn_clear);
        editSearch = findViewById(R.id.edit_search);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        chipAll = findViewById(R.id.chip_all);
        chipProjects = findViewById(R.id.chip_projects);
        chipTasks = findViewById(R.id.chip_tasks);
        chipMembers = findViewById(R.id.chip_members);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerResults = findViewById(R.id.recycler_results);
        emptyState = findViewById(R.id.empty_state);
        noResultsState = findViewById(R.id.no_results_state);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupAdapter() {
        adapter = new SearchResultAdapter(this, new SearchResultAdapter.OnSearchResultClickListener() {
            @Override
            public void onProjectClick(ProjectResponse project) {
                Intent intent = new Intent(SearchActivity.this, ProjectDetailActivity.class);
                intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, project.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskClick(TaskResponse task) {
                Intent intent = new Intent(SearchActivity.this, TaskDetailActivity.class);
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
                startActivity(intent);
            }

            @Override
            public void onUserClick(UserResponseDto user) {
                Toast.makeText(SearchActivity.this, 
                    user.getFirstName() + " " + user.getLastName(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            editSearch.setText("");
            currentQuery = "";
            showEmptyState();
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                // Debounce search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentQuery = query;
                    if (query.isEmpty()) {
                        showEmptyState();
                    } else {
                        performSearch();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentQuery = editSearch.getText().toString().trim();
                if (!currentQuery.isEmpty()) {
                    performSearch();
                }
                return true;
            }
            return false;
        });

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) {
                currentFilter = "all";
            } else if (checkedIds.contains(R.id.chip_projects)) {
                currentFilter = "projects";
            } else if (checkedIds.contains(R.id.chip_tasks)) {
                currentFilter = "tasks";
            } else if (checkedIds.contains(R.id.chip_members)) {
                currentFilter = "members";
            }
            
            if (!currentQuery.isEmpty()) {
                performSearch();
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        swipeRefresh.setOnRefreshListener(() -> {
            if (!currentQuery.isEmpty()) {
                performSearch();
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void performSearch() {
        if (currentQuery.isEmpty()) {
            showEmptyState();
            return;
        }

        showLoading(true);
        adapter.clearResults();

        // Search based on filter
        switch (currentFilter) {
            case "projects":
                searchProjects();
                break;
            case "tasks":
                searchTasks();
                break;
            case "members":
                searchMembers();
                break;
            default:
                searchAll();
                break;
        }
    }

    private void searchAll() {
        final int[] completedCalls = {0};
        final int totalCalls = 3;

        // Search projects
        long userId = prefsManager.getUserId();
        apiService.getProjectsByOwner(userId, 0, 20).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call,
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> filtered = filterProjects(response.body().getContent());
                    adapter.addProjects(filtered);
                }
                checkSearchComplete(++completedCalls[0], totalCalls);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                checkSearchComplete(++completedCalls[0], totalCalls);
            }
        });

        // Search member projects
        apiService.getProjectsByMember(userId, 0, 20).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call,
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> filtered = filterProjects(response.body().getContent());
                    adapter.addProjects(filtered);
                }
                checkSearchComplete(++completedCalls[0], totalCalls);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                checkSearchComplete(++completedCalls[0], totalCalls);
            }
        });

        // Search tasks
        apiService.getAllTasks(0, 50, userId, null, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call,
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> filtered = filterTasks(response.body().getContent());
                    adapter.addTasks(filtered);
                }
                checkSearchComplete(++completedCalls[0], totalCalls);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                checkSearchComplete(++completedCalls[0], totalCalls);
            }
        });
    }

    private void searchProjects() {
        long userId = prefsManager.getUserId();
        
        apiService.getProjectsByOwner(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call,
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> filtered = filterProjects(response.body().getContent());
                    adapter.addProjects(filtered);
                }
                
                // Also search member projects
                apiService.getProjectsByMember(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call,
                                           @NonNull Response<PageResponse<ProjectResponse>> response) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<ProjectResponse> filtered = filterProjects(response.body().getContent());
                            adapter.addProjects(filtered);
                        }
                        updateResultsVisibility();
                    }

                    @Override
                    public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        updateResultsVisibility();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchTasks() {
        long userId = prefsManager.getUserId();
        
        apiService.getAllTasks(0, 100, userId, null, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call,
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> filtered = filterTasks(response.body().getContent());
                    adapter.addTasks(filtered);
                }
                updateResultsVisibility();
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMembers() {
        apiService.getUsers(0, 50).enqueue(new Callback<PageResponse<UserResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<UserResponseDto>> call,
                                   @NonNull Response<PageResponse<UserResponseDto>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<UserResponseDto> filtered = filterUsers(response.body().getContent());
                    adapter.addUsers(filtered);
                }
                updateResultsVisibility();
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<UserResponseDto>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSearchComplete(int completed, int total) {
        if (completed >= total) {
            showLoading(false);
            swipeRefresh.setRefreshing(false);
            updateResultsVisibility();
        }
    }

    private List<ProjectResponse> filterProjects(List<ProjectResponse> projects) {
        if (projects == null) return new ArrayList<>();
        List<ProjectResponse> filtered = new ArrayList<>();
        String queryLower = currentQuery.toLowerCase();
        for (ProjectResponse project : projects) {
            if (project.getName() != null && project.getName().toLowerCase().contains(queryLower)) {
                filtered.add(project);
            } else if (project.getDescription() != null && project.getDescription().toLowerCase().contains(queryLower)) {
                filtered.add(project);
            }
        }
        return filtered;
    }

    private List<TaskResponse> filterTasks(List<TaskResponse> tasks) {
        if (tasks == null) return new ArrayList<>();
        List<TaskResponse> filtered = new ArrayList<>();
        String queryLower = currentQuery.toLowerCase();
        for (TaskResponse task : tasks) {
            if (task.getName() != null && task.getName().toLowerCase().contains(queryLower)) {
                filtered.add(task);
            } else if (task.getDescription() != null && task.getDescription().toLowerCase().contains(queryLower)) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    private List<UserResponseDto> filterUsers(List<UserResponseDto> users) {
        if (users == null) return new ArrayList<>();
        List<UserResponseDto> filtered = new ArrayList<>();
        String queryLower = currentQuery.toLowerCase();
        for (UserResponseDto user : users) {
            String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
            if (fullName.contains(queryLower)) {
                filtered.add(user);
            } else if (user.getEmail() != null && user.getEmail().toLowerCase().contains(queryLower)) {
                filtered.add(user);
            }
        }
        return filtered;
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        noResultsState.setVisibility(View.GONE);
        recyclerResults.setVisibility(View.GONE);
    }

    private void updateResultsVisibility() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.GONE);
            noResultsState.setVisibility(View.VISIBLE);
            recyclerResults.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            noResultsState.setVisibility(View.GONE);
            recyclerResults.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
