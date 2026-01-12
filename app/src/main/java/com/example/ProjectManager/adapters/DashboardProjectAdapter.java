package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.ProjectResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying projects in the dashboard.
 * Shows project name, description, and owner/member badge.
 */
public class DashboardProjectAdapter extends RecyclerView.Adapter<DashboardProjectAdapter.ProjectViewHolder> {

    private List<ProjectItem> projects = new ArrayList<>();
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(ProjectItem project);
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<ProjectItem> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_dashboard, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectName;
        private final TextView tvProjectDescription;
        private final TextView tvBadge;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvProjectDescription = itemView.findViewById(R.id.tv_project_description);
            tvBadge = itemView.findViewById(R.id.tv_badge);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(projects.get(pos));
                }
            });
        }

        void bind(ProjectItem project) {
            tvProjectName.setText(project.getName());
            
            String description = project.getDescription();
            if (description != null && !description.isEmpty()) {
                tvProjectDescription.setText(description);
                tvProjectDescription.setVisibility(View.VISIBLE);
            } else {
                tvProjectDescription.setText("No description");
                tvProjectDescription.setVisibility(View.VISIBLE);
            }

            // Set badge based on ownership
            if (project.isOwner()) {
                tvBadge.setText("Owner");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_owner);
                tvBadge.setTextColor(itemView.getContext().getColor(R.color.primary));
            } else {
                tvBadge.setText("Member");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_member);
                tvBadge.setTextColor(itemView.getContext().getColor(R.color.info));
            }
        }
    }

    /**
     * Data class for project items in the dashboard.
     */
    public static class ProjectItem {
        private long id;
        private String name;
        private String description;
        private boolean isOwner;

        public ProjectItem(long id, String name, String description, boolean isOwner) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.isOwner = isOwner;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isOwner() { return isOwner; }
    }
}
