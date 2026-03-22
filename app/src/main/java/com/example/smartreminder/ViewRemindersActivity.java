package com.example.smartreminder;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewRemindersActivity extends AppCompatActivity {

    private ReminderDatabaseHelper dbHelper;
    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        RecyclerView recyclerView = findViewById(R.id.recyclerReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new ReminderDatabaseHelper(this);
        List<Reminder> reminders = dbHelper.getAllReminders();

        adapter = new ReminderAdapter(reminders, this::confirmAndDeleteReminder);
        recyclerView.setAdapter(adapter);
    }

    private void confirmAndDeleteReminder(Reminder reminder, int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedRows = dbHelper.deleteReminderById(reminder.getId());
                    if (deletedRows > 0) {
                        adapter.removeReminderAt(position);
                        Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete reminder", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
