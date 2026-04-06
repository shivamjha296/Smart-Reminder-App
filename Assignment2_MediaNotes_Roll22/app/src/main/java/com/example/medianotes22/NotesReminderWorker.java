package com.example.medianotes22;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotesReminderWorker extends Worker {

    public NotesReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotesDatabaseHelper dbHelper = new NotesDatabaseHelper(getApplicationContext());
        int noteCount = dbHelper.getNoteCount();

        if (noteCount <= 0) {
            return Result.success();
        }

        createChannelIfNeeded();
        showNotification(noteCount);
        return Result.success();
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AssignmentConfig.getNotificationChannelId(),
                    "Media Notes Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Periodic note review reminders");

            NotificationManager manager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(int noteCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String message = AssignmentConfig.getNotificationMessage();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                AssignmentConfig.getNotificationChannelId()
        )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Reminder")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        message + ". You currently have " + noteCount + " saved notes."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(getApplicationContext()).notify(5000, builder.build());
    }
}
