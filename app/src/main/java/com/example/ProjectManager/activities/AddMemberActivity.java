package com.example.ProjectManager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.MemberAdapter;
import com.example.ProjectManager.database.ProjectDatabaseHelper;
import com.example.ProjectManager.models.Member;

import java.util.ArrayList;
import java.util.List;

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
    private ProjectDatabaseHelper databaseHelper;
    private List<Member> preSelectedMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_member);

        // Initialize database helper
        databaseHelper = new ProjectDatabaseHelper(this);

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
     * Load available members from database
     */
    private void loadMembers() {
        // TODO(API): Replace this with ApiService.getUsers(pageable) when backend is
        // ready.
        // For now we use a hardcoded list of users (mock data) so app works standalone.
        List<Member> members = getSampleMembers();

        memberAdapter.setMembers(members);

        // Set pre-selected members
        if (!preSelectedMembers.isEmpty()) {
            memberAdapter.setPreSelectedMembers(preSelectedMembers);
        }
    }

    /**
     * Get sample members for demonstration
     */
    private List<Member> getSampleMembers() {
        List<Member> members = new ArrayList<>();
        members.add(new Member(1, "Ivankov", "Sr Front End Developer"));
        members.add(new Member(2, "Brahm", "Mid Front End Developer"));
        members.add(new Member(3, "Alice", "Sr Front End Developer"));
        members.add(new Member(4, "Jeane", "Jr Front End Developer"));
        members.add(new Member(5, "Claudia", "Jr Front End Developer"));
        return members;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
