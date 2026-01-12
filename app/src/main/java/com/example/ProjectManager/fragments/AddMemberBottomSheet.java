package com.example.ProjectManager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.adapters.MemberAdapter;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private ApiService apiService;

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
     * Load the list of available members from API
     * GET /api/v1/users
     */
    private void loadMembers() {
        // Initialize API service if not already
        if (apiService == null && getContext() != null) {
            apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        }

        if (apiService == null) {
            // Fallback to sample data if API not available
            memberAdapter.setMembers(getSampleMembers());
            return;
        }

        // Disable select button while loading
        btnSelect.setEnabled(false);
        btnSelect.setText("Loading...");

        // Fetch users from API
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
                            String fullName = user.getFirstName();
                            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                                fullName += " " + user.getLastName();
                            }
                            Member member = new Member(
                                    user.getId().intValue(),
                                    fullName,
                                    user.getEmail() // Use email as role/subtitle
                            );
                            members.add(member);
                        }
                    }

                    if (members.isEmpty()) {
                        // No users from API, use sample data
                        members = getSampleMembers();
                    }

                    memberAdapter.setMembers(members);

                    // Set pre-selected members if any
                    if (!preSelectedMembers.isEmpty()) {
                        memberAdapter.setPreSelectedMembers(preSelectedMembers);
                    }
                } else {
                    // API error, fallback to sample data
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load users: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                    memberAdapter.setMembers(getSampleMembers());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<UserResponseDto>> call, Throwable t) {
                btnSelect.setEnabled(true);
                btnSelect.setText(R.string.select);

                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                // Fallback to sample data
                memberAdapter.setMembers(getSampleMembers());
            }
        });
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
        // Cleanup if needed
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
