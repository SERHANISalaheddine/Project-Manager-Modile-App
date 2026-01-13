package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.ProjectResponse;

import java.util.List;

public class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.ViewHolder> {

    private final List<ProjectResponse> projects;
    private final OnProjectClickListener listener;
    private Long currentUserId;

    public interface OnProjectClickListener {
        void onProjectClick(ProjectResponse project);
    }

    public ProjectCardAdapter(List<ProjectResponse> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectResponse project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvProjectName;
        private final TextView tvProjectDescription;
        private final TextView tvBadge;
        private final TextView tvProgress;
        private final ProgressBar progressBar;
        private final TextView tvTasksCount;
        private final TextView tvDoneCount;
        private final TextView tvInProgressCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvProjectDescription = itemView.findViewById(R.id.tv_project_description);
            tvBadge = itemView.findViewById(R.id.tv_badge);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            tvTasksCount = itemView.findViewById(R.id.tv_tasks_count);
            tvDoneCount = itemView.findViewById(R.id.tv_done_count);
            tvInProgressCount = itemView.findViewById(R.id.tv_in_progress_count);
        }

        void bind(ProjectResponse project) {
            tvProjectName.setText(project.getName() != null ? project.getName() : "Untitled Project");
            tvProjectDescription.setText(project.getDescription() != null ? 
                    project.getDescription() : "No description");

            // Since we don't have tasks list in ProjectResponse, show placeholder data
            // The actual progress will be calculated from API if needed
            int progress = 0; // Default progress
            tvProgress.setText(progress + "%");
            progressBar.setProgress(progress);

            // Task counts - will be populated if API returns them
            tvTasksCount.setText("- tasks");
            tvDoneCount.setText("- done");
            tvInProgressCount.setText("- active");

            // Badge - check if user is owner or member based on ownerId
            if (currentUserId != null && project.getOwnerId() != null 
                    && currentUserId.equals(project.getOwnerId())) {
                tvBadge.setText("Owner");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_owner);
            } else {
                tvBadge.setText("Member");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_member);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }
    }

    public void updateProjects(List<ProjectResponse> newProjects) {
        projects.clear();
        projects.addAll(newProjects);
        notifyDataSetChanged();
    }
}
