package com.example.ProjectManager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.TaskResponse;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    private final List<TaskResponse> items = new ArrayList<>();
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    public TaskAdapter() {
    }

    public TaskAdapter(List<TaskResponse> tasks, OnTaskClickListener listener) {
        if (tasks != null) items.addAll(tasks);
        this.listener = listener;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TaskResponse> tasks) {
        items.clear();
        if (tasks != null) items.addAll(tasks);
        notifyDataSetChanged();
    }

    static class TaskVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus;
        ProgressBar pbProgress;

        TaskVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            pbProgress = itemView.findViewById(R.id.pbProgress);
        }
    }

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {
        TaskResponse task = items.get(position);

        // 🔹 Title
        holder.tvTitle.setText(task.getName());

        // 🔹 Fake date (UI only)
        holder.tvDate.setText("27 Sept");

        // 🔹 Status + UI logic
        String status = task.getStatus();
        holder.tvStatus.setText(formatStatus(status));

        switch (status) {
            case "TODO":
                holder.pbProgress.setProgress(10);
                holder.tvStatus.setTextColor(Color.parseColor("#6C4DFF"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_todo);
                break;

            case "IN_PROGRESS":
                holder.pbProgress.setProgress(60);
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_progress);
                break;

            case "DONE":
                holder.pbProgress.setProgress(100);
                holder.tvStatus.setTextColor(Color.parseColor("#22C55E"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_done);
                break;

            case "ARCHIVED":
                holder.pbProgress.setProgress(0);
                holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_archived);
                break;
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatStatus(String status) {
        switch (status) {
            case "TODO": return "To Do";
            case "IN_PROGRESS": return "In Progress";
            case "DONE": return "Done";
            case "ARCHIVED": return "Archived";
            default: return status;
        }
    }
}
