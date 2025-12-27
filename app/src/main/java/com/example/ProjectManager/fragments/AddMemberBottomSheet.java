package com.example.ProjectManager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.MemberAdapter;
import com.example.ProjectManager.database.ProjectDatabaseHelper;
import com.example.ProjectManager.models.Member;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet dialog for selecting project members.
 * Displays a list of available members with selection capability.
 */
public class AddMemberBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRE_SELECTED_MEMBERS = "pre_selected_members";

    private RecyclerView rvMembers;
    private Button btnCancel;
    private Button btnSelect;
    private MemberAdapter memberAdapter;
    private OnMembersSelectedListener listener;
    private List<Member> preSelectedMembers;
    private ProjectDatabaseHelper databaseHelper;

    /**
     * Interface for communicating selected members back to the activity
     */
    public interface OnMembersSelectedListener {
        void onMembersSelected(List<Member> selectedMembers);
    }

    /**
     * Create a new instance of the bottom sheet with pre-selected members
     */
    public static AddMemberBottomSheet newInstance(ArrayList<Member> preSelectedMembers) {
        AddMemberBottomSheet fragment = new AddMemberBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRE_SELECTED_MEMBERS, preSelectedMembers);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create a new instance without pre-selected members
     */
    public static AddMemberBottomSheet newInstance() {
        return new AddMemberBottomSheet();
    }

    /**
     * Set the listener for member selection callbacks
     */
    public void setOnMembersSelectedListener(OnMembersSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get pre-selected members from arguments
        if (getArguments() != null) {
            preSelectedMembers = (ArrayList<Member>) getArguments().getSerializable(ARG_PRE_SELECTED_MEMBERS);
        }
        if (preSelectedMembers == null) {
            preSelectedMembers = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup button listeners
        setupButtonListeners();

        // Load sample members (replace with actual data source)
        loadMembers();
    }

    /**
     * Initialize view references
     */
    private void initViews(View view) {
        rvMembers = view.findViewById(R.id.rv_members);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSelect = view.findViewById(R.id.btn_select);
    }

    /**
     * Setup the RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter();
        memberAdapter.setMultiSelectEnabled(true);

        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.setAdapter(memberAdapter);
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupButtonListeners() {
        // Cancel button - dismiss without saving
        btnCancel.setOnClickListener(v -> dismiss());

        // Select button - pass selected members back and dismiss
        btnSelect.setOnClickListener(v -> {
            if (listener != null) {
                List<Member> selectedMembers = memberAdapter.getSelectedMembers();
                listener.onMembersSelected(selectedMembers);
            }
            dismiss();
        });
    }

    /**
     * Load the list of available members
     * Fetches from database or falls back to sample data
     */
    private void loadMembers() {
        // Initialize database helper if not already
        if (databaseHelper == null && getContext() != null) {
            databaseHelper = new ProjectDatabaseHelper(getContext());
        }

        List<Member> members;
        if (databaseHelper != null) {
            members = databaseHelper.getAllMembers();
            // If no members in database, use sample data
            if (members.isEmpty()) {
                members = getSampleMembers();
            }
        } else {
            members = getSampleMembers();
        }

        memberAdapter.setMembers(members);

        // Set pre-selected members if any
        if (!preSelectedMembers.isEmpty()) {
            memberAdapter.setPreSelectedMembers(preSelectedMembers);
        }
    }

    /**
     * Get sample members for demonstration
     * Used as fallback if database is empty
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
    public void onDestroyView() {
        super.onDestroyView();
        // Close database connection
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
