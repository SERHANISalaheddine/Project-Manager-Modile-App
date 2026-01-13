package com.example.ProjectManager.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.TaskResponse;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskCardAdapter extends RecyclerView.Adapter<TaskCardAdapter.ViewHolder> {

    private final List<TaskResponse> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    public TaskCardAdapter(List<TaskResponse> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskResponse task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final View statusIndicator;
        private final TextView tvTaskTitle;
        private final TextView tvTaskDescription;
        private final TextView tvStatusBadge;
        private final TextView tvProjectName;
        private final TextView tvPriority;
        private final TextView tvDueDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
        }

        void bind(TaskResponse task) {
            tvTaskTitle.setText(task.getTitle() != null ? task.getTitle() : "Untitled Task");
            tvTaskDescription.setText(task.getDescription() != null ? 
                    task.getDescription() : "No description");

            // Status
            String status = task.getStatus() != null ? task.getStatus() : "TODO";
            setupStatus(status);

            // Project name
            if (task.getProject() != null && task.getProject().getName() != null) {
                tvProjectName.setText(task.getProject().getName());
            } else {
                tvProjectName.setText("Project #" + task.getProjectId());
            }

            // Priority
            String priority = task.getPriority() != null ? task.getPriority() : "MEDIUM";
            setupPriority(priority);

            // Due date
            if (task.getDueDateAsDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                tvDueDate.setText("Due: " + sdf.format(task.getDueDateAsDate()));
            } else {
                tvDueDate.setText("No due date");
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }

        private void setupStatus(String status) {
            int color;
            String label;

            switch (status.toUpperCase().replace(" ", "_")) {
                case "IN_PROGRESS":
                case "IN PROGRESS":
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_in_progress);
                    label = "In Progress";
                    break;
                case "DONE":
                case "COMPLETED":
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_done);
                    label = "Done";
                    break;
                case "TODO":
                case "TO_DO":
                case "TO DO":
                default:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_todo);
                    label = "To Do";
                    break;
            }

            // Status indicator bar
            statusIndicator.setBackgroundColor(color);

            // Status badge
            tvStatusBadge.setText(label);
            tvStatusBadge.setTextColor(color);
            
            // Create background with matching color
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(dpToPx(8));
            background.setColor(adjustAlpha(color, 0.15f));
            tvStatusBadge.setBackground(background);
        }

        private void setupPriority(String priority) {
            int color;
            String label;

            switch (priority.toUpperCase()) {
                case "HIGH":
                case "URGENT":
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                    label = "High";
                    break;
                case "LOW":
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_low);
                    label = "Low";
                    break;
                case "MEDIUM":
                default:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
                    label = "Medium";
                    break;
            }

            tvPriority.setText(label);
            tvPriority.setTextColor(color);
            
            // Create background with matching color
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(dpToPx(6));
            background.setColor(adjustAlpha(color, 0.15f));
            tvPriority.setBackground(background);
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(255 * factor);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            return (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        private int dpToPx(int dp) {
            return (int) (dp * itemView.getContext().getResources().getDisplayMetrics().density);
        }
    }

    public void updateTasks(List<TaskResponse> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }
}
