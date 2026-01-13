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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ProjectManager.R;
import com.example.ProjectManager.models.dto.ProjectMemberResponse;
import com.example.ProjectManager.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProjectMemberAdapter extends RecyclerView.Adapter<ProjectMemberAdapter.MemberViewHolder> {

    private final Context context;
    private List<ProjectMemberResponse> members = new ArrayList<>();
    private long ownerId = -1;
    private boolean showRemoveButton = false;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onMemberClick(ProjectMemberResponse member);
        void onRemoveMember(ProjectMemberResponse member);
    }

    public ProjectMemberAdapter(Context context) {
        this.context = context;
    }

    public void setMembers(List<ProjectMemberResponse> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        notifyDataSetChanged();
    }

    public void setShowRemoveButton(boolean show) {
        this.showRemoveButton = show;
        notifyDataSetChanged();
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        ProjectMemberResponse member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtName;
        private final TextView txtEmail;
        private final TextView txtRole;
        private final ImageView btnRemove;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_member_avatar);
            txtName = itemView.findViewById(R.id.txt_member_name);
            txtEmail = itemView.findViewById(R.id.txt_member_email);
            txtRole = itemView.findViewById(R.id.txt_member_role);
            btnRemove = itemView.findViewById(R.id.btn_remove_member);
        }

        void bind(ProjectMemberResponse member) {
            // Set name
            String fullName = member.getFirstName() + " " + member.getLastName();
            txtName.setText(fullName);
            
            // Set email
            txtEmail.setText(member.getEmail());

            // Show role badge if owner
            if (member.getId() == ownerId) {
                txtRole.setVisibility(View.VISIBLE);
                txtRole.setText("Owner");
                txtRole.setBackgroundResource(R.drawable.bg_chip_primary);
            } else {
                txtRole.setVisibility(View.GONE);
            }

            // Load avatar - try profilePictureUrl first, then fallback to userId-based URL
            String imageUrl = ImageUtils.getProfilePictureUrl(member.getProfilePictureUrl());
            if (imageUrl == null && member.getId() != null) {
                // Backend doesn't provide profilePictureUrl, use userId-based endpoint
                imageUrl = ImageUtils.getProfilePictureUrlByUserId(member.getId());
            }
            
            if (imageUrl != null) {
                Glide.with(context)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Show/hide remove button (not for owner)
            if (showRemoveButton && member.getId() != ownerId) {
                btnRemove.setVisibility(View.VISIBLE);
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveMember(member);
                    }
                });
            } else {
                btnRemove.setVisibility(View.GONE);
            }

            // Item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberClick(member);
                }
            });
        }
    }
}
