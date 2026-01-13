package com.example.ProjectManager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;
import com.example.ProjectManager.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    private final List<TaskResponse> items = new ArrayList<>();
    private final List<TaskResponse> allItems = new ArrayList<>(); // Keep all items for filtering
    private OnTaskClickListener listener;
    private String currentFilter = null; // null = All
    
    // Cache for user names to avoid repeated API calls
    private static final LruCache<Long, String> userNameCache = new LruCache<>(50);

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    public TaskAdapter() {
    }

    public TaskAdapter(List<TaskResponse> tasks, OnTaskClickListener listener) {
        if (tasks != null) {
            items.addAll(tasks);
            allItems.addAll(tasks);
        }
        this.listener = listener;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TaskResponse> tasks) {
        allItems.clear();
        items.clear();
        if (tasks != null) {
            allItems.addAll(tasks);
            if (currentFilter == null) {
                items.addAll(tasks);
            } else {
                for (TaskResponse task : tasks) {
                    if (currentFilter.equals(task.getStatus())) {
                        items.add(task);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        currentFilter = status;
        items.clear();
        if (status == null) {
            items.addAll(allItems);
        } else {
            for (TaskResponse task : allItems) {
                if (status.equals(task.getStatus())) {
                    items.add(task);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getCountByStatus(String status) {
        if (status == null) return allItems.size();
        int count = 0;
        for (TaskResponse task : allItems) {
            if (status.equals(task.getStatus())) count++;
        }
        return count;
    }

    public int getTotalCount() {
        return allItems.size();
    }

    static class TaskVH extends RecyclerView.ViewHolder {
        View statusIndicator;
        TextView tvTitle, tvDescription, tvDate, tvStatus, tvAssigneeName, tvProjectName;
        CircleImageView imgAssignee;
        LinearLayout projectBadge;
        ProgressBar pbProgress;

        TaskVH(@NonNull View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAssigneeName = itemView.findViewById(R.id.tvAssigneeName);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            imgAssignee = itemView.findViewById(R.id.imgAssignee);
            projectBadge = itemView.findViewById(R.id.projectBadge);
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
        Context context = holder.itemView.getContext();

        // Title
        holder.tvTitle.setText(task.getName());

        // Description
        if (task.getContent() != null && !task.getContent().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getContent());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Date - use updatedAt or createdAt
        String dateStr = task.getUpdatedAt() != null ? task.getUpdatedAt() : task.getCreatedAt();
        if (dateStr != null) {
            holder.tvDate.setText(formatDate(dateStr));
        } else {
            holder.tvDate.setText("No date");
        }

        // Status + styling
        String status = task.getStatus();
        holder.tvStatus.setText(formatStatus(status));
        applyStatusStyle(holder, status);

        // Assignee - check both assignee object (RichTaskResponse) and direct userId (TaskResponse)
        if (task.hasAssigneeDetails()) {
            // We have full assignee details from RichTaskResponse
            TaskResponse.Assignee assignee = task.getAssignee();
            String fullName = "";
            if (assignee.getFirstName() != null) fullName += assignee.getFirstName();
            if (assignee.getLastName() != null) fullName += " " + assignee.getLastName();
            fullName = fullName.trim();
            
            if (fullName.isEmpty()) {
                fullName = assignee.getEmail() != null ? assignee.getEmail() : "Unassigned";
            }
            holder.tvAssigneeName.setText(fullName);

            // Load avatar - try profilePictureUrl first, then fallback to userId-based URL
            String imageUrl = ImageUtils.getProfilePictureUrl(assignee.getProfilePictureUrl());
            if (imageUrl == null && assignee.getId() != null) {
                // Backend doesn't provide profilePictureUrl in AssigneeDto, use userId-based endpoint
                imageUrl = ImageUtils.getProfilePictureUrlByUserId(assignee.getId());
            }
            
            if (imageUrl != null) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(holder.imgAssignee);
            } else {
                holder.imgAssignee.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else if (task.getAssigneeId() != null) {
            // Backend returned only userId (TaskResponse), fetch user details async
            Long userId = task.getAssigneeId();
            holder.tvAssigneeName.setText("Loading...");
            
            // Load avatar immediately using userId
            String avatarUrl = ImageUtils.getProfilePictureUrlByUserId(userId);
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(holder.imgAssignee);
            
            // Fetch user name async
            loadUserName(context, userId, holder.tvAssigneeName, task);
        } else {
            holder.tvAssigneeName.setText("Unassigned");
            holder.imgAssignee.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Project name (if available)
        if (task.getProjectName() != null && !task.getProjectName().isEmpty()) {
            holder.projectBadge.setVisibility(View.VISIBLE);
            holder.tvProjectName.setText(task.getProjectName());
        } else {
            holder.projectBadge.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    private void applyStatusStyle(@NonNull TaskVH holder, String status) {
        switch (status) {
            case "TODO":
                holder.statusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_todo);
                holder.tvStatus.setTextColor(Color.parseColor("#6366F1"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_todo);
                break;

            case "IN_PROGRESS":
                holder.statusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_progress);
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_progress);
                break;

            case "DONE":
                holder.statusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_done);
                holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_done);
                break;

            case "ARCHIVED":
                holder.statusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_archived);
                holder.tvStatus.setTextColor(Color.parseColor("#94A3B8"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_archived);
                break;

            default:
                holder.statusIndicator.setBackgroundResource(R.drawable.bg_status_indicator_todo);
                holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_archived);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case "TODO": return "To Do";
            case "IN_PROGRESS": return "In Progress";
            case "DONE": return "Done";
            case "ARCHIVED": return "Archived";
            default: return status;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        try {
            // Try ISO format first
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            // Try alternative format
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                Date date = inputFormat2.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (Exception e2) {
                // Return first 10 chars if all else fails
                if (dateStr.length() > 10) {
                    return dateStr.substring(0, 10);
                }
            }
        }
        return dateStr;
    }
    
    /**
     * Load user name from API and cache it for future use.
     * Also populates the assignee object in TaskResponse for consistency.
     */
    private void loadUserName(Context context, Long userId, TextView textView, TaskResponse task) {
        // Check cache first
        String cachedName = userNameCache.get(userId);
        if (cachedName != null) {
            textView.setText(cachedName);
            return;
        }
        
        ApiService apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        apiService.getUser(userId).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto user = response.body();
                    String fullName = "";
                    if (user.getFirstName() != null) fullName += user.getFirstName();
                    if (user.getLastName() != null) fullName += " " + user.getLastName();
                    fullName = fullName.trim();
                    
                    if (fullName.isEmpty()) {
                        fullName = user.getEmail() != null ? user.getEmail() : "Unknown";
                    }
                    
                    // Cache the name
                    userNameCache.put(userId, fullName);
                    
                    // Update UI
                    textView.setText(fullName);
                    
                    // Populate assignee object in task for future use
                    TaskResponse.Assignee assignee = new TaskResponse.Assignee();
                    assignee.setId(userId);
                    assignee.setFirstName(user.getFirstName());
                    assignee.setLastName(user.getLastName());
                    assignee.setEmail(user.getEmail());
                    task.setAssignee(assignee);
                } else {
                    textView.setText("Unknown");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                textView.setText("Unknown");
            }
        });
    }
}
