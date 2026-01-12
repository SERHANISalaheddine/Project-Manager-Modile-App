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

    public void setItems(List<Member> data) {
        items.clear();
        if (data != null) items.addAll(data);
        selectedPos = -1;
        notifyDataSetChanged();
    }

    public Member getSelected() {
        if (selectedPos >= 0 && selectedPos < items.size()) return items.get(selectedPos);
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

        View.OnClickListener select = v -> {
            selectedPos = holder.getAdapterPosition();
            notifyDataSetChanged();
        };

        holder.itemView.setOnClickListener(select);
        holder.rb.setOnClickListener(select);
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

