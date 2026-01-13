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
import com.example.ProjectManager.models.dto.TaskResponse;
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
    private Button btnCreate;

    // ---------- API ----------
    private ApiService api;

    // ---------- State ----------
    private long projectId = 1;        // passed from Intent
    private long selectedUserId = -1;  // chosen member id
    private String selectedStatus = "TODO";

    private final List<Member> memberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // ✅ 1) API init (same as TaskActivity)
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        // ✅ 2) Read projectId from Intent if exists
        if (getIntent() != null && getIntent().hasExtra("projectId")) {
            projectId = getIntent().getLongExtra("projectId", 1);
        }

        // ✅ 3) Bind views (match your XML IDs)
        etTaskName = findViewById(R.id.etName);
        etTaskContent = findViewById(R.id.etContent);

        tvSelectedStatus = findViewById(R.id.tvSelectedStatus);
        tvSelectedMember = findViewById(R.id.tvSelectedMember);

        btnCreate = findViewById(R.id.btnCreate);

        // Default UI
        tvSelectedStatus.setText("Select Status");
        tvSelectedMember.setText("Select Member");

        // Click listeners
        findViewById(R.id.btnStatus).setOnClickListener(v -> openStatusSheet());
        findViewById(R.id.btnAssignTo).setOnClickListener(v -> openAssignToSheet());

        btnCreate.setOnClickListener(v -> submitCreateTask());

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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

                    // ⚠️ IMPORTANT: adjust if PageResponse uses a different method name
                    List<ProjectMemberResponse> users = response.body().getContent();

                    memberList.clear();
                    if (users != null) {
                        for (ProjectMemberResponse u : users) {
                            memberList.add(MemberMapper.fromProjectMember(u));
                        }
                    }

                    adapter.setItems(memberList);

                } else {
                    Toast.makeText(CreateTaskActivity.this,
                            "Failed to load members (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProjectMemberResponse>> call, Throwable t) {
                Toast.makeText(CreateTaskActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
