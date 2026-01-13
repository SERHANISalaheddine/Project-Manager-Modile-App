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
import com.example.ProjectManager.adapters.ProjectCardAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.utils.NavigationUtils;
import com.example.ProjectManager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectsActivity extends AppCompatActivity {

    private static final String TAG = "ProjectsActivity";

    // UI Components
    private EditText etSearch;
    private ImageView btnCreateProject;
    private TabLayout tabFilter;
    private TextView tvTotalCount, tvOwnedCount, tvMemberCount;
    private RecyclerView rvProjects;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;

    // Bottom Navigation
    private LinearLayout navHome, navProjects, navTasks, navProfile;

    // Data
    private ApiService apiService;
    private SessionManager sessionManager;
    private ProjectCardAdapter adapter;
    private List<ProjectResponse> allProjects = new ArrayList<>();
    private List<ProjectResponse> ownedProjects = new ArrayList<>();
    private List<ProjectResponse> memberProjects = new ArrayList<>();
    private List<ProjectResponse> displayedProjects = new ArrayList<>();

    private String currentFilter = "all"; // all, owned, member
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        initViews();
        setupServices();
        setupRecyclerView();
        setupTabFilter();
        setupSearch();
        setupNavigation();
        setupCreateButton();

        loadProjects();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnCreateProject = findViewById(R.id.btn_create_project);
        tabFilter = findViewById(R.id.tab_filter);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvOwnedCount = findViewById(R.id.tv_owned_count);
        tvMemberCount = findViewById(R.id.tv_member_count);
        rvProjects = findViewById(R.id.rv_projects);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.layout_empty);

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
            swipeRefresh.setOnRefreshListener(this::loadProjects);
        }
    }

    private void setupServices() {
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
    }

    private void setupRecyclerView() {
        adapter = new ProjectCardAdapter(displayedProjects, project -> {
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra("project_id", project.getId());
            startActivity(intent);
        });
        
        // Set current user ID for owner/member badge display
        Long userId = sessionManager.getUserId();
        if (userId != null) {
            adapter.setCurrentUserId(userId);
        }

        if (rvProjects != null) {
            rvProjects.setLayoutManager(new LinearLayoutManager(this));
            rvProjects.setAdapter(adapter);
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
                        currentFilter = "owned";
                        break;
                    case 2:
                        currentFilter = "member";
                        break;
                }
                filterAndDisplayProjects();
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
                filterAndDisplayProjects();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupNavigation() {
        NavigationUtils.updateNavigation(navHome, navProjects, navTasks, navProfile, "projects");
        NavigationUtils.setupNavigationListeners(this, navHome, navProjects, navTasks, navProfile);
    }

    private void setupCreateButton() {
        if (btnCreateProject == null) return;
        
        btnCreateProject.setOnClickListener(v -> {
            // Open create project activity
            Intent intent = new Intent(ProjectsActivity.this, CreateProjectActivity.class);
            startActivity(intent);
        });
    }

    private void loadProjects() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        // Load owned projects
        loadOwnedProjects(userId);
    }

    private void loadOwnedProjects(Long userId) {
        apiService.getProjectsByOwner(userId, 0, 100).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call, 
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ownedProjects.clear();
                    ownedProjects.addAll(response.body().getContent());
                    Log.d(TAG, "Loaded " + ownedProjects.size() + " owned projects");
                    
                    // Now load member projects
                    loadMemberProjects(userId);
                } else {
                    Log.e(TAG, "Failed to load owned projects: " + response.code());
                    loadMemberProjects(userId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading owned projects", t);
                loadMemberProjects(userId);
            }
        });
    }

    private void loadMemberProjects(Long userId) {
        apiService.getProjectsByMember(userId, 0, 100).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectResponse>> call, 
                                   @NonNull Response<PageResponse<ProjectResponse>> response) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    memberProjects.clear();
                    memberProjects.addAll(response.body().getContent());
                    Log.d(TAG, "Loaded " + memberProjects.size() + " member projects");
                }
                
                combineAndDisplayProjects();
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading member projects", t);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                combineAndDisplayProjects();
            }
        });
    }

    private void combineAndDisplayProjects() {
        // Combine all projects (avoiding duplicates by ID)
        allProjects.clear();
        allProjects.addAll(ownedProjects);
        
        for (ProjectResponse memberProject : memberProjects) {
            boolean exists = false;
            for (ProjectResponse owned : ownedProjects) {
                if (owned.getId().equals(memberProject.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allProjects.add(memberProject);
            }
        }

        // Update stats
        updateStats();
        
        // Filter and display
        filterAndDisplayProjects();
    }

    private void updateStats() {
        if (tvTotalCount != null) {
            tvTotalCount.setText(String.valueOf(allProjects.size()));
        }
        if (tvOwnedCount != null) {
            tvOwnedCount.setText(String.valueOf(ownedProjects.size()));
        }
        if (tvMemberCount != null) {
            tvMemberCount.setText(String.valueOf(memberProjects.size()));
        }
    }

    private void filterAndDisplayProjects() {
        displayedProjects.clear();

        List<ProjectResponse> sourceList;
        switch (currentFilter) {
            case "owned":
                sourceList = ownedProjects;
                break;
            case "member":
                sourceList = memberProjects;
                break;
            default:
                sourceList = allProjects;
        }

        // Apply search filter
        if (searchQuery.isEmpty()) {
            displayedProjects.addAll(sourceList);
        } else {
            for (ProjectResponse project : sourceList) {
                if (matchesSearch(project)) {
                    displayedProjects.add(project);
                }
            }
        }

        // Update UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private boolean matchesSearch(ProjectResponse project) {
        if (project.getName() != null && project.getName().toLowerCase().contains(searchQuery)) {
            return true;
        }
        if (project.getDescription() != null && project.getDescription().toLowerCase().contains(searchQuery)) {
            return true;
        }
        return false;
    }

    private void updateEmptyState() {
        if (emptyState == null || rvProjects == null) return;
        
        if (displayedProjects.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvProjects.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvProjects.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }
}
