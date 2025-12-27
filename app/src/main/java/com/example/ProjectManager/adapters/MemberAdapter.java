package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of members in a RecyclerView.
 * Supports both single and multiple selection modes.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> members;
    private OnMemberClickListener listener;
    private boolean isMultiSelectEnabled;
    private boolean isDisplayMode; // For showing selected members without interaction

    /**
     * Interface for handling member item clicks
     */
    public interface OnMemberClickListener {
        void onMemberClick(Member member, int position);
    }

    /**
     * Default constructor with empty member list
     */
    public MemberAdapter() {
        this.members = new ArrayList<>();
        this.isMultiSelectEnabled = true;
        this.isDisplayMode = false;
    }

    /**
     * Constructor with member list
     */
    public MemberAdapter(List<Member> members) {
        this.members = members != null ? members : new ArrayList<>();
        this.isMultiSelectEnabled = true;
        this.isDisplayMode = false;
    }

    /**
     * Constructor with member list and click listener
     */
    public MemberAdapter(List<Member> members, OnMemberClickListener listener) {
        this(members);
        this.listener = listener;
    }

    /**
     * Set the click listener
     */
    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    /**
     * Enable or disable multi-select mode
     */
    public void setMultiSelectEnabled(boolean enabled) {
        this.isMultiSelectEnabled = enabled;
    }

    /**
     * Enable display mode (no interaction, just showing selected members)
     */
    public void setDisplayMode(boolean displayMode) {
        this.isDisplayMode = displayMode;
    }

    /**
     * Update the member list
     */
    public void setMembers(List<Member> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Get current member list
     */
    public List<Member> getMembers() {
        return members;
    }

    /**
     * Get list of selected members
     */
    public List<Member> getSelectedMembers() {
        List<Member> selectedMembers = new ArrayList<>();
        for (Member member : members) {
            if (member.isSelected()) {
                selectedMembers.add(member);
            }
        }
        return selectedMembers;
    }

    /**
     * Get count of selected members
     */
    public int getSelectedCount() {
        int count = 0;
        for (Member member : members) {
            if (member.isSelected()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Toggle selection state of a member
     */
    public void toggleSelection(int position) {
        if (position >= 0 && position < members.size()) {
            Member member = members.get(position);
            member.setSelected(!member.isSelected());
            notifyItemChanged(position);
        }
    }

    /**
     * Clear all selections
     */
    public void clearSelections() {
        for (Member member : members) {
            member.setSelected(false);
        }
        notifyDataSetChanged();
    }

    /**
     * Select all members
     */
    public void selectAll() {
        for (Member member : members) {
            member.setSelected(true);
        }
        notifyDataSetChanged();
    }

    /**
     * Set pre-selected members based on a list
     */
    public void setPreSelectedMembers(List<Member> preSelectedMembers) {
        if (preSelectedMembers == null)
            return;

        for (Member member : members) {
            member.setSelected(preSelectedMembers.contains(member));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    /**
     * ViewHolder class for member items
     */
    class MemberViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMemberName;
        private RadioButton rbMemberSelect;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            rbMemberSelect = itemView.findViewById(R.id.rb_member_select);

            // Set click listener on the entire item
            itemView.setOnClickListener(v -> {
                if (!isDisplayMode) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Member member = members.get(position);

                        // Toggle selection
                        toggleSelection(position);

                        // Notify listener
                        if (listener != null) {
                            listener.onMemberClick(member, position);
                        }
                    }
                }
            });
        }

        /**
         * Bind member data to the view
         */
        public void bind(Member member) {
            // Set member display text (Name - Role)
            tvMemberName.setText(member.getDisplayText());

            // Set radio button state
            rbMemberSelect.setChecked(member.isSelected());

            // Hide radio button in display mode
            if (isDisplayMode) {
                rbMemberSelect.setVisibility(View.GONE);
            } else {
                rbMemberSelect.setVisibility(View.VISIBLE);
            }
        }
    }
}
