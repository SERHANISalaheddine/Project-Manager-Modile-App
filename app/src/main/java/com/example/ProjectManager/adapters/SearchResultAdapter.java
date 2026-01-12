package com.example.ProjectManager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UserResponseDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROJECT = 0;
    private static final int TYPE_TASK = 1;
    private static final int TYPE_USER = 2;
    private static final int TYPE_SECTION = 3;

    private final Context context;
    private final List<Object> items = new ArrayList<>();
    private final Set<Long> addedProjectIds = new HashSet<>();
    private final Set<Long> addedTaskIds = new HashSet<>();
    private final Set<Long> addedUserIds = new HashSet<>();
    private final OnSearchResultClickListener listener;

    public interface OnSearchResultClickListener {
        void onProjectClick(ProjectResponse project);
        void onTaskClick(TaskResponse task);
        void onUserClick(UserResponseDto user);
    }

    public SearchResultAdapter(Context context, OnSearchResultClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void clearResults() {
        items.clear();
        addedProjectIds.clear();
        addedTaskIds.clear();
        addedUserIds.clear();
        notifyDataSetChanged();
    }

    public void addProjects(List<ProjectResponse> projects) {
        if (projects == null || projects.isEmpty()) return;
        
        boolean needsSection = true;
        for (Object item : items) {
            if (item instanceof String && "Projects".equals(item)) {
                needsSection = false;
                break;
            }
        }
        
        List<ProjectResponse> newProjects = new ArrayList<>();
        for (ProjectResponse project : projects) {
            if (!addedProjectIds.contains(project.getId())) {
                addedProjectIds.add(project.getId());
                newProjects.add(project);
            }
        }
        
        if (!newProjects.isEmpty()) {
            if (needsSection) {
                items.add("Projects");
            }
            items.addAll(newProjects);
            notifyDataSetChanged();
        }
    }

    public void addTasks(List<TaskResponse> tasks) {
        if (tasks == null || tasks.isEmpty()) return;
        
        boolean needsSection = true;
        for (Object item : items) {
            if (item instanceof String && "Tasks".equals(item)) {
                needsSection = false;
                break;
            }
        }
        
        List<TaskResponse> newTasks = new ArrayList<>();
        for (TaskResponse task : tasks) {
            if (!addedTaskIds.contains(task.getId())) {
                addedTaskIds.add(task.getId());
                newTasks.add(task);
            }
        }
        
        if (!newTasks.isEmpty()) {
            if (needsSection) {
                items.add("Tasks");
            }
            items.addAll(newTasks);
            notifyDataSetChanged();
        }
    }

    public void addUsers(List<UserResponseDto> users) {
        if (users == null || users.isEmpty()) return;
        
        boolean needsSection = true;
        for (Object item : items) {
            if (item instanceof String && "Members".equals(item)) {
                needsSection = false;
                break;
            }
        }
        
        List<UserResponseDto> newUsers = new ArrayList<>();
        for (UserResponseDto user : users) {
            if (!addedUserIds.contains(user.getId())) {
                addedUserIds.add(user.getId());
                newUsers.add(user);
            }
        }
        
        if (!newUsers.isEmpty()) {
            if (needsSection) {
                items.add("Members");
            }
            items.addAll(newUsers);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return TYPE_SECTION;
        if (item instanceof ProjectResponse) return TYPE_PROJECT;
        if (item instanceof TaskResponse) return TYPE_TASK;
        if (item instanceof UserResponseDto) return TYPE_USER;
        return TYPE_SECTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case TYPE_PROJECT:
                return new ProjectViewHolder(inflater.inflate(R.layout.item_search_project, parent, false));
            case TYPE_TASK:
                return new TaskViewHolder(inflater.inflate(R.layout.item_search_task, parent, false));
            case TYPE_USER:
                return new UserViewHolder(inflater.inflate(R.layout.item_search_user, parent, false));
            default:
                return new SectionViewHolder(inflater.inflate(R.layout.item_search_section, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        
        if (holder instanceof ProjectViewHolder && item instanceof ProjectResponse) {
            ((ProjectViewHolder) holder).bind((ProjectResponse) item);
        } else if (holder instanceof TaskViewHolder && item instanceof TaskResponse) {
            ((TaskViewHolder) holder).bind((TaskResponse) item);
        } else if (holder instanceof UserViewHolder && item instanceof UserResponseDto) {
            ((UserViewHolder) holder).bind((UserResponseDto) item);
        } else if (holder instanceof SectionViewHolder && item instanceof String) {
            ((SectionViewHolder) holder).bind((String) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolders
    class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSection;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSection = itemView.findViewById(R.id.txt_section);
        }

        void bind(String section) {
            txtSection.setText(section);
        }
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgIcon;
        private final TextView txtName;
        private final TextView txtDescription;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_icon);
            txtName = itemView.findViewById(R.id.txt_name);
            txtDescription = itemView.findViewById(R.id.txt_description);
        }

        void bind(ProjectResponse project) {
            txtName.setText(project.getName());
            txtDescription.setText(project.getDescription() != null ? project.getDescription() : "No description");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View statusIndicator;
        private final TextView txtName;
        private final TextView txtStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
            txtName = itemView.findViewById(R.id.txt_name);
            txtStatus = itemView.findViewById(R.id.txt_status);
        }

        void bind(TaskResponse task) {
            txtName.setText(task.getName());
            
            String status = task.getStatus();
            txtStatus.setText(formatStatus(status));
            
            // Set status color
            int color;
            switch (status != null ? status : "") {
                case "IN_PROGRESS":
                    color = context.getResources().getColor(R.color.status_in_progress, null);
                    break;
                case "DONE":
                    color = context.getResources().getColor(R.color.status_done, null);
                    break;
                case "ARCHIVED":
                    color = context.getResources().getColor(R.color.text_muted, null);
                    break;
                default:
                    color = context.getResources().getColor(R.color.status_todo, null);
            }
            statusIndicator.setBackgroundColor(color);
            txtStatus.setTextColor(color);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }

        private String formatStatus(String status) {
            if (status == null) return "TODO";
            switch (status) {
                case "IN_PROGRESS": return "In Progress";
                case "DONE": return "Done";
                case "ARCHIVED": return "Archived";
                default: return "To Do";
            }
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtName;
        private final TextView txtEmail;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtName = itemView.findViewById(R.id.txt_name);
            txtEmail = itemView.findViewById(R.id.txt_email);
        }

        void bind(UserResponseDto user) {
            txtName.setText(user.getFirstName() + " " + user.getLastName());
            txtEmail.setText(user.getEmail());
            
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                String imageUrl = user.getProfilePictureUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "http://10.0.2.2:8080" + imageUrl;
                }
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}
