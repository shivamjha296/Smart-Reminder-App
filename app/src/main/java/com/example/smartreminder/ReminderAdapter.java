package com.example.smartreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminders;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClicked(Reminder reminder, int position);
    }

    public ReminderAdapter(List<Reminder> reminders, OnDeleteClickListener deleteClickListener) {
        this.reminders = reminders;
        this.deleteClickListener = deleteClickListener;
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
        holder.tvTime.setText("Date & Time: " + reminder.getDate() + " " + reminder.getTime());
        holder.tvLocation.setText("Location: " + reminder.getLocation());
        holder.tvCategory.setText("Category: " + reminder.getCategory());
        holder.tvAlertType.setText("Alert: " + reminder.getAlertType());

        boolean expired = reminder.isExpired();
        holder.tvStatus.setText(expired ? "Expired" : "Upcoming");
        int statusColor = ContextCompat.getColor(
            holder.itemView.getContext(),
            expired ? R.color.status_expired : R.color.status_upcoming
        );
        holder.tvStatus.setTextColor(statusColor);

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClicked(reminder, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void removeReminderAt(int position) {
        if (position < 0 || position >= reminders.size()) {
            return;
        }
        reminders.remove(position);
        notifyItemRemoved(position);
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvTime;
        TextView tvLocation;
        TextView tvCategory;
        TextView tvAlertType;
        TextView tvStatus;
        Button btnDelete;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            tvLocation = itemView.findViewById(R.id.tvItemLocation);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvAlertType = itemView.findViewById(R.id.tvItemAlertType);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
        }
    }
}
