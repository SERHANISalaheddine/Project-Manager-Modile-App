package com.example.ProjectManager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.ProjectMemberAdapter;
import com.example.ProjectManager.adapters.TaskAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectMemberResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Modern Project Detail Activity with tabs for Tasks and Members.
 */
public class ProjectDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    
    // Views
    private ImageView btnBack;
    private ImageView btnEdit;
    private ImageView btnDelete;
    private TextView txtProjectName;
    private TextView txtProjectDescription;
    private TextView txtStartDate;
    private TextView txtEndDate;
    private TextView txtOwner;
    private LinearLayout progressContainer;
    private ProgressBar progressBar;
    private TextView txtProgressPercent;
    private TextView txtTaskStats;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerContent;
    private View emptyState;
    private TextView txtEmptyTitle;
    private TextView txtEmptySubtitle;
    private ExtendedFloatingActionButton fabAdd;
    private View loadingOverlay;
    
    // Adapters
    private TaskAdapter taskAdapter;
    private ProjectMemberAdapter memberAdapter;
    
    // Data
    private ApiService apiService;
    private SharedPrefsManager prefsManager;
    private long projectId;
    private long currentUserId;
    private ProjectResponse currentProject;
    private List<TaskResponse> tasks = new ArrayList<>();
    private List<ProjectMemberResponse> members = new ArrayList<>();
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (projectId == -1) {
            Toast.makeText(this, "Invalid project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();

        initViews();
        setupAdapters();
        setupClickListeners();
        setupTabs();
        setupBackPressHandler();
        
        loadProjectDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        txtProjectName = findViewById(R.id.txt_project_name);
        txtProjectDescription = findViewById(R.id.txt_project_description);
        txtStartDate = findViewById(R.id.txt_start_date);
        txtEndDate = findViewById(R.id.txt_end_date);
        txtOwner = findViewById(R.id.txt_owner);
        progressContainer = findViewById(R.id.progress_container);
        progressBar = findViewById(R.id.progress_bar);
        txtProgressPercent = findViewById(R.id.txt_progress_percent);
        txtTaskStats = findViewById(R.id.txt_task_stats);
        tabLayout = findViewById(R.id.tab_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerContent = findViewById(R.id.recycler_content);
        emptyState = findViewById(R.id.empty_state);
        txtEmptyTitle = findViewById(R.id.txt_empty_title);
        txtEmptySubtitle = findViewById(R.id.txt_empty_subtitle);
        fabAdd = findViewById(R.id.fab_add);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupAdapters() {
        taskAdapter = new TaskAdapter(tasks, task -> {
            // Open task detail
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
            startActivity(intent);
        });
        
        memberAdapter = new ProjectMemberAdapter(this);
        memberAdapter.setOnMemberActionListener(new ProjectMemberAdapter.OnMemberActionListener() {
            @Override
            public void onMemberClick(ProjectMemberResponse member) {
                // Show member info
                Toast.makeText(ProjectDetailActivity.this, 
                    member.getFirstName() + " " + member.getLastName(), 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoveMember(ProjectMemberResponse member) {
                showRemoveMemberConfirmation(member);
            }
        });
        
        recyclerContent.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProjectActivity.class);
            intent.putExtra(EditProjectActivity.EXTRA_PROJECT_ID, projectId);
            startActivity(intent);
        });
        
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        swipeRefresh.setOnRefreshListener(this::loadProjectDetails);
        
        fabAdd.setOnClickListener(v -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                // Add task
                Intent intent = new Intent(this, CreateTaskActivity.class);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            } else {
                // Add member
                Intent intent = new Intent(this, AddMemberActivity.class);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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

    private void loadProjectDetails() {
        showLoading(true);
        
        apiService.getProject(projectId).enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectResponse> call, 
                                   @NonNull Response<ProjectResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProject = response.body();
                    displayProjectInfo();
                    loadTasks();
                    loadMembers();
                    
                    // Show/hide edit/delete buttons based on ownership
                    boolean isOwner = currentProject.getOwnerId() == currentUserId;
                    btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                    btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                } else {
                    showLoading(false);
                    Toast.makeText(ProjectDetailActivity.this, "Failed to load project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectResponse> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ProjectDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProjectInfo() {
        txtProjectName.setText(currentProject.getName());
        
        String description = currentProject.getDescription();
        if (description != null && !description.isEmpty()) {
            txtProjectDescription.setText(description);
            txtProjectDescription.setVisibility(View.VISIBLE);
        } else {
            txtProjectDescription.setVisibility(View.GONE);
        }
        
        if (currentProject.getStartDate() != null) {
            txtStartDate.setText("Start: " + dateFormat.format(currentProject.getStartDate()));
        }
        
        if (currentProject.getEndDate() != null) {
            txtEndDate.setText("End: " + dateFormat.format(currentProject.getEndDate()));
        }
        
        // Load owner info
        apiService.getUser(currentProject.getOwnerId()).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, 
                                   @NonNull Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String ownerName = response.body().getFirstName() + " " + response.body().getLastName();
                    txtOwner.setText("Owner: " + ownerName.trim());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {}
        });
    }

    private void loadTasks() {
        apiService.getAllTasks(0, 100, null, projectId, null).enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<TaskResponse>> call,
                                   @NonNull Response<PageResponse<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tasks.clear();
                    tasks.addAll(response.body().getContent());
                    updateProgress();
                    
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        updateContent(0);
                    }
                }
                showLoading(false);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<TaskResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadMembers() {
        apiService.getProjectMembers(projectId, 0, 100).enqueue(new Callback<PageResponse<ProjectMemberResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProjectMemberResponse>> call,
                                   @NonNull Response<PageResponse<ProjectMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    members.clear();
                    members.addAll(response.body().getContent());
                    
                    // Update adapter with new data
                    memberAdapter.setMembers(members);
                    if (currentProject != null) {
                        memberAdapter.setOwnerId(currentProject.getOwnerId());
                        // Show remove button only for owner (except for owner themselves)
                        memberAdapter.setShowRemoveButton(currentProject.getOwnerId() == currentUserId);
                    }
                    
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        updateContent(1);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ProjectMemberResponse>> call, @NonNull Throwable t) {}
        });
    }

    private void updateProgress() {
        if (tasks.isEmpty()) {
            progressContainer.setVisibility(View.GONE);
            return;
        }
        
        progressContainer.setVisibility(View.VISIBLE);
        
        int total = tasks.size();
        int done = 0;
        int inProgress = 0;
        
        for (TaskResponse task : tasks) {
            if ("DONE".equals(task.getStatus())) {
                done++;
            } else if ("IN_PROGRESS".equals(task.getStatus())) {
                inProgress++;
            }
        }
        
        int percent = (int) ((done * 100.0) / total);
        progressBar.setProgress(percent);
        txtProgressPercent.setText(percent + "%");
        txtTaskStats.setText(done + "/" + total + " tasks completed");
    }

    private void updateContent(int tabPosition) {
        if (tabPosition == 0) {
            // Tasks tab
            recyclerContent.setAdapter(taskAdapter);
            taskAdapter.notifyDataSetChanged();
            fabAdd.setText("Add Task");
            fabAdd.setIconResource(R.drawable.ic_add);
            
            if (tasks.isEmpty()) {
                showEmptyState("No Tasks Yet", "Create your first task to get started");
            } else {
                hideEmptyState();
            }
        } else {
            // Members tab
            recyclerContent.setAdapter(memberAdapter);
            memberAdapter.notifyDataSetChanged();
            fabAdd.setText("Add Member");
            fabAdd.setIconResource(R.drawable.ic_person_add);
            
            // Only show add member if owner
            if (currentProject != null && currentProject.getOwnerId() == currentUserId) {
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                fabAdd.setVisibility(View.GONE);
            }
            
            if (members.isEmpty()) {
                showEmptyState("No Members Yet", "Add team members to collaborate");
            } else {
                hideEmptyState();
            }
        }
    }

    private void showMemberOptions(ProjectMemberResponse member) {
        // Don't allow removing the owner
        if (member.getId().equals(currentProject.getOwnerId())) {
            Toast.makeText(this, "Cannot remove project owner", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle(member.getFirstName() + " " + member.getLastName())
            .setItems(new String[]{"Remove from project"}, (dialog, which) -> {
                if (which == 0) {
                    removeMember(member);
                }
            })
            .show();
    }

    private void showRemoveMemberConfirmation(ProjectMemberResponse member) {
        // Don't allow removing the owner
        if (member.getId().equals(currentProject.getOwnerId())) {
            Toast.makeText(this, "Cannot remove project owner", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Remove Member")
            .setMessage("Are you sure you want to remove " + member.getFirstName() + " " + member.getLastName() + " from this project?")
            .setPositiveButton("Remove", (dialog, which) -> removeMember(member))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void removeMember(ProjectMemberResponse member) {
        showLoading(true);
        
        apiService.removeMemberFromProject(projectId, member.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProjectDetailActivity.this, "Member removed", Toast.LENGTH_SHORT).show();
                    loadMembers();
                } else {
                    Toast.makeText(ProjectDetailActivity.this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(ProjectDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete this project? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteProject())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteProject() {
        showLoading(true);
        
        apiService.deleteProject(projectId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProjectDetailActivity.this, "Project deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProjectDetailActivity.this, "Failed to delete project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(ProjectDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState(String title, String subtitle) {
        emptyState.setVisibility(View.VISIBLE);
        recyclerContent.setVisibility(View.GONE);
        txtEmptyTitle.setText(title);
        txtEmptySubtitle.setText(subtitle);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerContent.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentProject != null) {
            loadProjectDetails();
        }
    }
}
