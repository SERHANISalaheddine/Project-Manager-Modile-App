package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.TaskResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying tasks in the dashboard.
 * Shows task name, project name, and status badge with color coding.
 */
public class DashboardTaskAdapter extends RecyclerView.Adapter<DashboardTaskAdapter.TaskViewHolder> {

    private List<TaskItem> tasks = new ArrayList<>();
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskItem task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskItem> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_dashboard, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItem task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View viewStatusIndicator;
        private final TextView tvTaskName;
        private final TextView tvProjectName;
        private final TextView tvStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvStatus = itemView.findViewById(R.id.tv_status);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(pos));
                }
            });
        }

        void bind(TaskItem task) {
            tvTaskName.setText(task.getName());
            tvProjectName.setText(task.getProjectName() != null ? task.getProjectName() : "Unknown Project");

            // Set status badge and indicator color
            String status = task.getStatus();
            switch (status) {
                case "IN_PROGRESS":
                    tvStatus.setText("In Progress");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_progress);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_in_progress));
                    viewStatusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_progress);
                    break;
                case "DONE":
                    tvStatus.setText("Done");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_done);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_done));
                    viewStatusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_done);
                    break;
                case "ARCHIVED":
                    tvStatus.setText("Archived");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_todo);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_archived));
                    viewStatusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_todo);
                    break;
                case "TODO":
                default:
                    tvStatus.setText("To Do");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_todo);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_todo));
                    viewStatusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_todo);
                    break;
            }
        }
    }

    /**
     * Data class for task items in the dashboard.
     */
    public static class TaskItem {
        private long id;
        private String name;
        private String status;
        private long projectId;
        private String projectName;

        public TaskItem(long id, String name, String status, long projectId, String projectName) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.projectId = projectId;
            this.projectName = projectName;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public long getProjectId() { return projectId; }
        public String getProjectName() { return projectName; }
    }
}
