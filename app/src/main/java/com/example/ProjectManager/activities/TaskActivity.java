package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ApiService apiService;

    private Button createTaskBtn;

    // Optional: if you want to always filter tasks by a project
    // private long projectId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks); // your layout with taskRecyclerView + createTaskBtn

        // --- RecyclerView ---
        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // --- API ---
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // --- Create Task Button ---
        createTaskBtn = findViewById(R.id.createTaskBtn);
        createTaskBtn.setOnClickListener(v -> {
            Intent i = new Intent(TaskActivity.this, CreateTaskActivity.class);

            // If you want to pass projectId to CreateTaskActivity:
            // i.putExtra("projectId", projectId);

            startActivity(i);
        });

        // First load
        loadTasksFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when coming back from CreateTaskActivity
        loadTasksFromBackend();
    }

    private void loadTasksFromBackend() {
        // Example: page 0 size 20, no filters
        Call<PageResponse<TaskResponse>> call = apiService.getAllTasks(
                0, 20,
                null,  // userId filter
                null,  // projectId filter
                null   // status filter ("IN_PROGRESS" etc.)
        );

        call.enqueue(new Callback<PageResponse<TaskResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<TaskResponse>> call,
                                   Response<PageResponse<TaskResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    // IMPORTANT: adjust if your PageResponse uses another field name
                    // usually: getContent() / getItems() / getData()
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
