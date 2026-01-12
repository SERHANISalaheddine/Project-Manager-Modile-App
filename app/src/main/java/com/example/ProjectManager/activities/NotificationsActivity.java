package com.example.ProjectManager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.NotificationAdapter;
import com.example.ProjectManager.models.NotificationItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Notifications Activity - displays user notifications.
 * Note: Backend notification endpoints are not yet implemented.
 * This activity uses mock data for demonstration.
 */
public class NotificationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private TextView txtEmptyTitle;
    private TextView txtEmptySubtitle;

    private NotificationAdapter adapter;
    private List<NotificationItem> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
        setupBackPressHandler();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerNotifications = findViewById(R.id.recycler_notifications);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);
        txtEmptyTitle = findViewById(R.id.txt_empty_title);
        txtEmptySubtitle = findViewById(R.id.txt_empty_subtitle);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notifications, notification -> {
            // Handle notification click
            notification.setRead(true);
            adapter.notifyDataSetChanged();
        });
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void loadNotifications() {
        swipeRefresh.setRefreshing(true);

        // TODO: Replace with actual API call when backend is ready
        // For now, show mock data or empty state
        
        // Simulate network delay
        swipeRefresh.postDelayed(() -> {
            swipeRefresh.setRefreshing(false);
            
            // Mock data for demonstration
            notifications.clear();
            
            // Add some sample notifications
            notifications.add(new NotificationItem(
                1,
                "Task Assigned",
                "You have been assigned to 'Implement login feature'",
                "task_assigned",
                new Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                false
            ));
            
            notifications.add(new NotificationItem(
                2,
                "Project Update",
                "Project 'Mobile App' deadline has been updated",
                "project_update",
                new Date(System.currentTimeMillis() - 86400000), // 1 day ago
                true
            ));
            
            notifications.add(new NotificationItem(
                3,
                "Task Completed",
                "Task 'Setup database' has been marked as done",
                "task_completed",
                new Date(System.currentTimeMillis() - 172800000), // 2 days ago
                true
            ));
            
            if (notifications.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                adapter.notifyDataSetChanged();
            }
        }, 500);
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerNotifications.setVisibility(View.GONE);
        txtEmptyTitle.setText("No Notifications");
        txtEmptySubtitle.setText("You're all caught up! Check back later for updates.");
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerNotifications.setVisibility(View.VISIBLE);
    }
}
