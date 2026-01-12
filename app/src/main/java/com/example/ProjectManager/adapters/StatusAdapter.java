package com.example.ProjectManager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectManager.R;
import com.example.ProjectManager.models.StatusItem;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.VH> {

    private final List<StatusItem> items;
    private int selectedPos = -1;

    public StatusAdapter(List<StatusItem> items) {
        this.items = items;
    }

    public StatusItem getSelected() {
        if (selectedPos >= 0 && selectedPos < items.size()) return items.get(selectedPos);
        return null;
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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_radio_option, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StatusItem item = items.get(position);
        holder.tvLabel.setText(item.label);

        holder.rb.setChecked(position == selectedPos);

        holder.itemView.setOnClickListener(v -> {
            selectedPos = holder.getAdapterPosition();
            notifyDataSetChanged();
        });

        holder.rb.setOnClickListener(v -> {
            selectedPos = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

