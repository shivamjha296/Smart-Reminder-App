package com.example.smartreminder;

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

public class ReminderWorker extends Worker {

    private static final String CHANNEL_ID = "reminder_channel_22";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(getApplicationContext());
        int reminderCount = dbHelper.getReminderCount();

        if (reminderCount > 0) {
            createChannelIfNeeded();
            showNotification(reminderCount);
        }
        return Result.success();
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Periodic reminder checks");
            NotificationManager manager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(int reminderCount) {
        String assignmentMessage = "Reminder: Check your scheduled task";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Smart Reminder App")
                .setContentText(assignmentMessage + " (Total reminders: " + reminderCount + ")")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        NotificationManagerCompat.from(getApplicationContext()).notify(2201, builder.build());
    }
}
