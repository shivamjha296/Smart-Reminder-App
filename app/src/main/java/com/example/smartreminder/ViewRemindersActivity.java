package com.example.smartreminder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewRemindersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        RecyclerView recyclerView = findViewById(R.id.recyclerReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        List<Reminder> reminders = dbHelper.getAllReminders();

        ReminderAdapter adapter = new ReminderAdapter(reminders);
        recyclerView.setAdapter(adapter);
    }
}
