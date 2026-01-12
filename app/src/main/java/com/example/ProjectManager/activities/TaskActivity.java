package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ApiService apiService;

    private Button createTaskBtn;
    private LinearLayout navHome, navCalendar, navTasks, navProfile;
    private TextView tvProjectName;

    // Project filter
    private long projectId = -1;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Initialize SharedPrefs Manager
        prefsManager = SharedPrefsManager.getInstance(this);

        // --- RecyclerView ---
        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // --- Project Name TextView ---
        tvProjectName = findViewById(R.id.tvProjectName);
        if (tvProjectName == null) {
            // Create one if not in layout
            tvProjectName = new TextView(this);
        }

        // --- API ---
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // --- Get projectId from Intent ---
        if (getIntent() != null) {
            projectId = getIntent().getLongExtra("projectId", -1);
            String projectName = getIntent().getStringExtra("projectName");

            if (projectName != null && tvProjectName != null) {
                tvProjectName.setText(projectName);
            }

            // Save this project as the last opened one
            if (projectId > 0) {
                prefsManager.saveLastProjectId(projectId);
            }
        }

        // Initialize navigation
        initializeNavigation();

        // --- Create Task Button ---
        createTaskBtn = findViewById(R.id.createTaskBtn);
        createTaskBtn.setOnClickListener(v -> {
            Intent i = new Intent(TaskActivity.this, CreateTaskActivity.class);
            if (projectId > 0) {
                i.putExtra("projectId", projectId);
            }
            startActivity(i);
        });

        // First load
        loadTasksFromBackend();
    }

    /**
     * Initialize navigation bar
     */
    private void initializeNavigation() {
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navTasks = findViewById(R.id.nav_tasks);
        navProfile = findViewById(R.id.nav_profile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                Toast.makeText(this, "Calendar feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {
                // Already on tasks, do nothing or refresh
                loadTasksFromBackend();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Update navigation to highlight tasks icon
        NavigationUtils.updateNavigation(navHome, navCalendar, navTasks, "tasks");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when coming back from CreateTaskActivity
        loadTasksFromBackend();
        // Update navigation again to ensure correct highlighting
        NavigationUtils.updateNavigation(navHome, navCalendar, navTasks, "tasks");
    }

    private void loadTasksFromBackend() {
        // Filter by projectId if available
        Call<PageResponse<TaskResponse>> call;

        if (projectId > 0) {
            // Filter tasks by project
            call = apiService.getAllTasks(
                    0, 50,
                    null, // userId filter
                    projectId, // projectId filter
                    null // status filter
            );
        } else {
            // Load all tasks (no project filter)
            call = apiService.getAllTasks(
                    0, 50,
                    null, // userId filter
                    null, // projectId filter
                    null // status filter
            );
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

                } else {
                    Toast.makeText(TaskActivity.this,
                            "Erreur chargement tasks: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<TaskResponse>> call, Throwable t) {
                Toast.makeText(TaskActivity.this,
                        "Connexion échouée: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
