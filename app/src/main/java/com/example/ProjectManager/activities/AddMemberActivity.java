package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.MemberAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for selecting project members.
 * Alternative to the bottom sheet dialog for member selection.
 * Displays a full-screen list of available members with multi-selection
 * support.
 */
public class AddMemberActivity extends AppCompatActivity {

    // Intent keys
    public static final String EXTRA_SELECTED_MEMBERS = "selected_members";
    public static final String EXTRA_PRE_SELECTED_MEMBERS = "pre_selected_members";

    // UI Components
    private RecyclerView rvMembers;
    private Button btnCancel;
    private Button btnSelect;

    // Data
    private MemberAdapter memberAdapter;
    private List<Member> preSelectedMembers;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_member);

        // Initialize API service
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // Get pre-selected members from intent
        if (getIntent() != null && getIntent().hasExtra(EXTRA_PRE_SELECTED_MEMBERS)) {
            preSelectedMembers = (ArrayList<Member>) getIntent().getSerializableExtra(EXTRA_PRE_SELECTED_MEMBERS);
        }
        if (preSelectedMembers == null) {
            preSelectedMembers = new ArrayList<>();
        }

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load members
        loadMembers();
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        rvMembers = findViewById(R.id.rv_members);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSelect = findViewById(R.id.btn_select);
    }

    /**
     * Setup the RecyclerView with adapter
     */
    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter();
        memberAdapter.setMultiSelectEnabled(true);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
    }

    /**
     * Setup button click listeners
     */
    private void setupListeners() {
        // Cancel button
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Select button
        btnSelect.setOnClickListener(v -> {
            // Get selected members and return them
            ArrayList<Member> selectedMembers = new ArrayList<>(memberAdapter.getSelectedMembers());

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_MEMBERS, selectedMembers);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Load available members from API
     */
    private void loadMembers() {
        // Show loading state
        btnSelect.setEnabled(false);
        btnSelect.setText("Loading...");

        // Make API call to fetch users
        Call<PageResponse<UserResponseDto>> call = apiService.getUsers(0, 50);
        call.enqueue(new Callback<PageResponse<UserResponseDto>>() {
            @Override
            public void onResponse(Call<PageResponse<UserResponseDto>> call,
                    Response<PageResponse<UserResponseDto>> response) {
                btnSelect.setEnabled(true);
                btnSelect.setText(R.string.select);

                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<UserResponseDto> pageResponse = response.body();
                    List<UserResponseDto> users = pageResponse.getContent();

                    // Convert UserResponseDto to Member objects
                    List<Member> members = new ArrayList<>();
                    if (users != null) {
                        for (UserResponseDto user : users) {
                            Member member = new Member(
                                    user.getId().intValue(),
                                    user.getFirstName() + " " + user.getLastName(),
                                    "" // No role from API response
                            );
                            members.add(member);
                        }
                    }

                    memberAdapter.setMembers(members);

                    // Set pre-selected members
                    if (!preSelectedMembers.isEmpty()) {
                        memberAdapter.setPreSelectedMembers(preSelectedMembers);
                    }
                } else {
                    Toast.makeText(AddMemberActivity.this,
                            "Failed to load users: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<UserResponseDto>> call, Throwable t) {
                btnSelect.setEnabled(true);
                btnSelect.setText(R.string.select);

                Toast.makeText(AddMemberActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
