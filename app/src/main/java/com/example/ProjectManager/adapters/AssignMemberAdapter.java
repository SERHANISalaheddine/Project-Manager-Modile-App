package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.Member;

import java.util.ArrayList;
import java.util.List;

public class AssignMemberAdapter extends RecyclerView.Adapter<AssignMemberAdapter.VH> {

    private final List<Member> items = new ArrayList<>();
    private int selectedPos = -1;
    private Member selectedMember = null;

    public void setItems(List<Member> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        // Restore selection position if we have a selected member
        if (selectedMember != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId() == selectedMember.getId()) {
                    selectedPos = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public Member getSelected() {
        if (selectedMember != null)
            return selectedMember;
        if (selectedPos >= 0 && selectedPos < items.size())
            return items.get(selectedPos);
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assign_member_radio, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Member m = items.get(position);

        holder.tvLabel.setText(m.getDisplayText());
        holder.rb.setChecked(position == selectedPos);

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION)
                return;

            int oldPos = selectedPos;
            selectedPos = adapterPos;
            selectedMember = items.get(selectedPos);

            // Only notify the changed items, not the whole dataset
            if (oldPos >= 0) {
                notifyItemChanged(oldPos);
            }
            notifyItemChanged(selectedPos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLabel;
        RadioButton rb;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            rb = itemView.findViewById(R.id.rb);
        }
    }
}
