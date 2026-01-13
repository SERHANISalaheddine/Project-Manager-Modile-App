package com.example.ProjectManager.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.AssignMemberAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.mappers.MemberMapper;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.dto.CreateTaskRequest;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectMemberResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CreateTaskActivity extends AppCompatActivity {

    // ---------- UI ----------
    private EditText etTaskName;
    private EditText etTaskContent;
    private TextView tvSelectedStatus;
    private TextView tvSelectedMember;
    private TextView tvSelectedProject;
    private Button btnCreate;
    private View btnProject;

    // ---------- API ----------
    private ApiService api;
    private SessionManager sessionManager;

    // ---------- State ----------
    private long projectId = -1;       // passed from Intent, -1 = not set
    private String projectName = null;
    private long selectedUserId = -1;  // chosen member id
    private String selectedStatus = "TODO";

    private final List<Member> memberList = new ArrayList<>();
    private final List<ProjectResponse> projectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // ✅ 1) API init (same as TaskActivity)
        api = RetrofitClient.getInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // ✅ 2) Read projectId from Intent if exists
        if (getIntent() != null && getIntent().hasExtra("projectId")) {
            projectId = getIntent().getLongExtra("projectId", -1);
            projectName = getIntent().getStringExtra("projectName");
        }

        // ✅ 3) Bind views (match your XML IDs)
        etTaskName = findViewById(R.id.etName);
        etTaskContent = findViewById(R.id.etContent);

        tvSelectedStatus = findViewById(R.id.tvSelectedStatus);
        tvSelectedMember = findViewById(R.id.tvSelectedMember);
        tvSelectedProject = findViewById(R.id.tvSelectedProject);
        btnProject = findViewById(R.id.btnProject);

        btnCreate = findViewById(R.id.btnCreate);

        // Default UI
        tvSelectedStatus.setText("Select Status");
        tvSelectedMember.setText("Select Member");
        
        if (projectId > 0 && projectName != null) {
            tvSelectedProject.setText(projectName);
            if (btnProject != null) btnProject.setVisibility(View.GONE);
        } else if (tvSelectedProject != null) {
            tvSelectedProject.setText("Select Project");
        }

        // Click listeners
        findViewById(R.id.btnStatus).setOnClickListener(v -> openStatusSheet());
        findViewById(R.id.btnAssignTo).setOnClickListener(v -> {
            if (projectId <= 0) {
                Toast.makeText(this, "Please select a project first", Toast.LENGTH_SHORT).show();
                return;
            }
            openAssignToSheet();
        });
        
        if (btnProject != null) {
            btnProject.setOnClickListener(v -> openProjectSheet());
        }

        btnCreate.setOnClickListener(v -> submitCreateTask());

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // -----------------------------
    // Project selection bottom sheet
    // -----------------------------
    private void openProjectSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_project_select, null);
        dialog.setContentView(sheet);

        RecyclerView rv = sheet.findViewById(R.id.rvProjects);
        Button btnCancel = sheet.findViewById(R.id.btnCancel);
        View loadingView = sheet.findViewById(R.id.loadingView);

        rv.setLayoutManager(new LinearLayoutManager(this));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Load projects
        loadProjects(rv, loadingView, dialog);

        dialog.show();
    }

    private void loadProjects(RecyclerView rv, View loadingView, BottomSheetDialog dialog) {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        projectList.clear();
        
        // First load owned projects, then load member projects
        api.getProjectsByOwner(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call,
                                   retrofit2.Response<PageResponse<ProjectResponse>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> ownedProjects = response.body().getContent();
                    if (ownedProjects != null) {
                        projectList.addAll(ownedProjects);
                    }
                }
                
                // Now load member projects
                loadMemberProjects(rv, loadingView, dialog, userId);
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                // Even if owned projects fail, try member projects
                loadMemberProjects(rv, loadingView, dialog, userId);
            }
        });
    }
    
    private void loadMemberProjects(RecyclerView rv, View loadingView, BottomSheetDialog dialog, long userId) {
        api.getProjectsByMember(userId, 0, 50).enqueue(new Callback<PageResponse<ProjectResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectResponse>> call,
                                   retrofit2.Response<PageResponse<ProjectResponse>> response) {
                if (loadingView != null) loadingView.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectResponse> memberProjects = response.body().getContent();
                    
                    // Add member projects that are not already in the list (avoid duplicates)
                    if (memberProjects != null) {
                        java.util.Set<Long> existingIds = new java.util.HashSet<>();
                        for (ProjectResponse p : projectList) {
                            existingIds.add(p.getId());
                        }
                        for (ProjectResponse p : memberProjects) {
                            if (!existingIds.contains(p.getId())) {
                                projectList.add(p);
                            }
                        }
                    }
                }
                
                // Display combined projects
                displayProjects(rv, dialog);
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectResponse>> call, Throwable t) {
                if (loadingView != null) loadingView.setVisibility(View.GONE);
                
                // Display whatever we have from owned projects
                displayProjects(rv, dialog);
            }
        });
    }
    
    private void displayProjects(RecyclerView rv, BottomSheetDialog dialog) {
        if (!projectList.isEmpty()) {
            ProjectSelectAdapter adapter = new ProjectSelectAdapter(projectList, project -> {
                projectId = project.getId();
                projectName = project.getName();
                tvSelectedProject.setText(project.getName());
                tvSelectedProject.setTextColor(0xFF1F1F1F);
                
                // Reset member selection when project changes
                selectedUserId = -1;
                tvSelectedMember.setText("Select Member");
                tvSelectedMember.setTextColor(0xFF9E9E9E);
                memberList.clear();
                
                dialog.dismiss();
            });
            rv.setAdapter(adapter);
        } else {
            Toast.makeText(CreateTaskActivity.this, 
                    "No projects found. Create a project first.", 
                    Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    // -----------------------------
    // Inner adapter for project selection
    // -----------------------------
    private static class ProjectSelectAdapter extends RecyclerView.Adapter<ProjectSelectAdapter.ViewHolder> {
        private final List<ProjectResponse> items;
        private final OnProjectSelectedListener listener;

        interface OnProjectSelectedListener {
            void onProjectSelected(ProjectResponse project);
        }

        ProjectSelectAdapter(List<ProjectResponse> items, OnProjectSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_project_select, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ProjectResponse project = items.get(position);
            holder.tvName.setText(project.getName());
            if (holder.tvDescription != null) {
                holder.tvDescription.setText(project.getDescription() != null ? 
                        project.getDescription() : "");
            }
            holder.itemView.setOnClickListener(v -> listener.onProjectSelected(project));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvDescription;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProjectName);
                tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            }
        }
    }

    // -----------------------------
    // Status bottom sheet
    // -----------------------------
    private void openStatusSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_status, null);
        dialog.setContentView(sheet);

        TextView optTodo = sheet.findViewById(R.id.optTodo);
        TextView optInProgress = sheet.findViewById(R.id.optInProgress);
        TextView optDone = sheet.findViewById(R.id.optDone);
        TextView optArchived = sheet.findViewById(R.id.optArchived);

        optTodo.setOnClickListener(v -> {
            selectedStatus = "TODO";
            tvSelectedStatus.setText("To Do");
            dialog.dismiss();
        });

        optInProgress.setOnClickListener(v -> {
            selectedStatus = "IN_PROGRESS";
            tvSelectedStatus.setText("In Progress");
            dialog.dismiss();
        });

        optDone.setOnClickListener(v -> {
            selectedStatus = "DONE";
            tvSelectedStatus.setText("Done");
            dialog.dismiss();
        });

        optArchived.setOnClickListener(v -> {
            selectedStatus = "ARCHIVED";
            tvSelectedStatus.setText("Archived");
            dialog.dismiss();
        });

        dialog.show();
    }

    // -----------------------------
    // Assign To bottom sheet
    // -----------------------------
    private void openAssignToSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_assign_to, null);
        dialog.setContentView(sheet);

        RecyclerView rv = sheet.findViewById(R.id.rvMembers);
        Button btnCancel = sheet.findViewById(R.id.btnCancel);
        Button btnSelect = sheet.findViewById(R.id.btnSelect);

        rv.setLayoutManager(new LinearLayoutManager(this));

        AssignMemberAdapter adapter = new AssignMemberAdapter();
        rv.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSelect.setOnClickListener(v -> {
            Member picked = adapter.getSelected();
            if (picked != null) {
                selectedUserId = picked.getId();
                tvSelectedMember.setText(picked.getName());
                tvSelectedMember.setTextColor(0xFF1F1F1F);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Select a member", Toast.LENGTH_SHORT).show();
            }
        });

        loadProjectMembers(adapter);

        dialog.show();
    }

    private void loadProjectMembers(AssignMemberAdapter adapter) {
        api.getProjectMembers(projectId, 0, 50).enqueue(new Callback<PageResponse<ProjectMemberResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProjectMemberResponse>> call,
                                   retrofit2.Response<PageResponse<ProjectMemberResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<ProjectMemberResponse> users = response.body().getContent();

                    memberList.clear();
                    if (users != null && !users.isEmpty()) {
                        for (ProjectMemberResponse u : users) {
                            memberList.add(MemberMapper.fromProjectMember(u));
                        }
                        adapter.setItems(memberList);
                    } else {
                        Toast.makeText(CreateTaskActivity.this,
                                "No members found in this project",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String errorMsg = "Failed to load members";
                    if (response.code() == 403) {
                        errorMsg = "You don't have access to this project's members";
                    } else if (response.code() == 404) {
                        errorMsg = "Project not found";
                    } else if (response.code() == 401) {
                        errorMsg = "Please login again";
                    }
                    Toast.makeText(CreateTaskActivity.this,
                            errorMsg + " (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectMemberResponse>> call, Throwable t) {
                Toast.makeText(CreateTaskActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // -----------------------------
    // Create task request
    // -----------------------------
    private void submitCreateTask() {
        String name = etTaskName.getText() != null ? etTaskName.getText().toString().trim() : "";
        String content = etTaskContent.getText() != null ? etTaskContent.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etTaskName.setError("Task name required");
            return;
        }

        if (projectId <= 0) {
            Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUserId <= 0) {
            Toast.makeText(this, "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateTaskRequest req = new CreateTaskRequest(
                name,
                content,
                selectedStatus,
                selectedUserId,
                projectId
        );

        btnCreate.setEnabled(false);

        api.createTask(req).enqueue(new retrofit2.Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, retrofit2.Response<TaskResponse> response) {
                btnCreate.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(CreateTaskActivity.this, "Task created", Toast.LENGTH_SHORT).show();
                    finish(); // returns to TaskActivity, which refreshes in onResume()
                } else {
                    Toast.makeText(CreateTaskActivity.this,
                            "Create failed (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                btnCreate.setEnabled(true);
                Toast.makeText(CreateTaskActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
