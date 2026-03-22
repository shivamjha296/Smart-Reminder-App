package com.example.smartreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.tvTitle.setText(reminder.getTitle());
        holder.tvDescription.setText("Description: " + reminder.getDescription());
        holder.tvTime.setText("Time: " + reminder.getTime());
        holder.tvLocation.setText("Location: " + reminder.getLocation());
        holder.tvCategory.setText("Category: " + reminder.getCategory());
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvTime;
        TextView tvLocation;
        TextView tvCategory;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            tvLocation = itemView.findViewById(R.id.tvItemLocation);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
        }
    }
}
