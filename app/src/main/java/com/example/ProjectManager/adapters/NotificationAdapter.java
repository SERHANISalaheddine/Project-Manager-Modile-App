package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public NotificationAdapter(List<NotificationItem> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgIcon;
        private View unreadIndicator;
        private TextView txtTitle;
        private TextView txtMessage;
        private TextView txtTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_icon);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTime = itemView.findViewById(R.id.txt_time);
        }

        public void bind(NotificationItem notification) {
            txtTitle.setText(notification.getTitle());
            txtMessage.setText(notification.getMessage());
            txtTime.setText(getRelativeTime(notification.getCreatedAt()));
            
            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
            
            // Set icon based on type
            int iconRes;
            switch (notification.getType()) {
                case "task_assigned":
                    iconRes = R.drawable.ic_task_assigned;
                    break;
                case "task_completed":
                    iconRes = R.drawable.ic_task_done;
                    break;
                case "project_update":
                    iconRes = R.drawable.ic_project;
                    break;
                default:
                    iconRes = R.drawable.ic_notification_primary;
            }
            imgIcon.setImageResource(iconRes);
            
            // Apply different background for unread
            if (!notification.isRead()) {
                itemView.setBackgroundResource(R.drawable.bg_notification_unread);
            } else {
                itemView.setBackgroundResource(0);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
        
        private String getRelativeTime(Date date) {
            if (date == null) return "";
            
            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            
            if (diffInMinutes < 1) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + "m ago";
            } else if (diffInHours < 24) {
                return diffInHours + "h ago";
            } else if (diffInDays < 7) {
                return diffInDays + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                return sdf.format(date);
            }
        }
    }
}
