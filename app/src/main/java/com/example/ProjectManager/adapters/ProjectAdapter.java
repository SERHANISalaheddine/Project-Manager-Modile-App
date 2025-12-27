package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.Project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying projects in a RecyclerView.
 * Shows project cards with title, member avatars, and due date.
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectClickListener listener;
    private SimpleDateFormat dateFormat;

    /**
     * Interface for handling project item clicks
     */
    public interface OnProjectClickListener {
        void onProjectClick(Project project, int position);

        void onProjectLongClick(Project project, int position);
    }

    /**
     * Default constructor
     */
    public ProjectAdapter() {
        this.projects = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    }

    /**
     * Constructor with project list
     */
    public ProjectAdapter(List<Project> projects) {
        this();
        this.projects = projects != null ? projects : new ArrayList<>();
    }

    /**
     * Constructor with project list and click listener
     */
    public ProjectAdapter(List<Project> projects, OnProjectClickListener listener) {
        this(projects);
        this.listener = listener;
    }

    /**
     * Set the click listener
     */
    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.listener = listener;
    }

    /**
     * Update the project list
     */
    public void setProjects(List<Project> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Add a project to the list
     */
    public void addProject(Project project) {
        if (project != null) {
            projects.add(0, project); // Add at the beginning
            notifyItemInserted(0);
        }
    }

    /**
     * Remove a project from the list
     */
    public void removeProject(int position) {
        if (position >= 0 && position < projects.size()) {
            projects.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Get project at position
     */
    public Project getProject(int position) {
        if (position >= 0 && position < projects.size()) {
            return projects.get(position);
        }
        return null;
    }

    /**
     * Get all projects
     */
    public List<Project> getProjects() {
        return projects;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    /**
     * ViewHolder class for project items
     */
    class ProjectViewHolder extends RecyclerView.ViewHolder {

        private View viewStatusIndicator;
        private TextView tvProjectTitle;
        private View viewProgressBar;
        private LinearLayout layoutMemberAvatars;
        private View avatar1;
        private View avatar2;
        private View avatar3;
        private TextView tvDueDate;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);

            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvProjectTitle = itemView.findViewById(R.id.tv_project_title);
            viewProgressBar = itemView.findViewById(R.id.view_progress_bar);
            layoutMemberAvatars = itemView.findViewById(R.id.layout_member_avatars);
            avatar1 = itemView.findViewById(R.id.avatar_1);
            avatar2 = itemView.findViewById(R.id.avatar_2);
            avatar3 = itemView.findViewById(R.id.avatar_3);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(projects.get(position), position);
                }
            });

            // Set long click listener
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectLongClick(projects.get(position), position);
                    return true;
                }
                return false;
            });
        }

        /**
         * Bind project data to the view
         */
        public void bind(Project project) {
            // Set project title
            tvProjectTitle.setText(project.getTitle());

            // Set due date
            if (project.getDueDate() != null) {
                tvDueDate.setText(dateFormat.format(project.getDueDate()));
                tvDueDate.setVisibility(View.VISIBLE);
            } else if (project.getCreatedAt() != null) {
                // Show created date if no due date
                tvDueDate.setText(dateFormat.format(project.getCreatedAt()));
                tvDueDate.setVisibility(View.VISIBLE);
            } else {
                tvDueDate.setVisibility(View.GONE);
            }

            // Setup member avatars visibility based on member count
            List<Member> members = project.getMembers();
            int memberCount = members != null ? members.size() : 0;

            avatar1.setVisibility(memberCount >= 1 ? View.VISIBLE : View.GONE);
            avatar2.setVisibility(memberCount >= 2 ? View.VISIBLE : View.GONE);
            avatar3.setVisibility(memberCount >= 3 ? View.VISIBLE : View.GONE);

            // Update status indicator color based on project status
            updateStatusIndicator(project.getStatus());
        }

        /**
         * Update the status indicator color based on project status
         */
        private void updateStatusIndicator(String status) {
            int colorRes;
            if (status == null) {
                colorRes = R.color.purple_primary;
            } else {
                switch (status.toLowerCase()) {
                    case "completed":
                        colorRes = R.color.teal_700;
                        break;
                    case "in_progress":
                        colorRes = R.color.badge_red;
                        break;
                    case "created":
                    default:
                        colorRes = R.color.purple_primary;
                        break;
                }
            }
            viewStatusIndicator.setBackgroundResource(colorRes);
        }
    }
}
