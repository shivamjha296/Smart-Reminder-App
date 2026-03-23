package com.example.smartreminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class ReminderWorker extends Worker {

    private static final String CHANNEL_ID = AssignmentConfig.getNotificationChannelId();

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(getApplicationContext());
        List<Reminder> dueReminders = dbHelper.getDueReminders(System.currentTimeMillis());

        if (dueReminders.isEmpty()) {
            return Result.success();
        }

        createChannelIfNeeded();
        for (Reminder reminder : dueReminders) {
            boolean handled;
            if ("RHYTHM".equalsIgnoreCase(reminder.getAlertType())) {
                handled = playRhythm();
            } else {
                handled = showNotification(reminder);
            }

            if (handled) {
                dbHelper.markReminderAsNotified(reminder.getId());
            }
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

        private boolean showNotification(Reminder reminder) {
        String assignmentMessage = AssignmentConfig.getNotificationMessage();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder: " + reminder.getTitle())
            .setContentText(assignmentMessage + " at " + reminder.getTime() + " on " + reminder.getDate())
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(reminder.getDescription() == null || reminder.getDescription().isEmpty()
                    ? assignmentMessage
                    : reminder.getDescription()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return false;
        }
        NotificationManagerCompat.from(getApplicationContext()).notify(2200 + reminder.getId(), builder.build());
        return true;
    }

    private boolean playRhythm() {
        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmTone == null) {
            alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmTone);
        if (ringtone == null) {
            return false;
        }

        ringtone.play();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
        }, 8000);
        return true;
    }
}
