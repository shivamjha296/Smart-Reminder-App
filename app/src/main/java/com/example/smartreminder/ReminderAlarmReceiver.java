package com.example.smartreminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_ALERT_TYPE = "extra_alert_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        if (reminderId <= 0) {
            return;
        }

        String title = intent.getStringExtra(EXTRA_TITLE);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);
        String date = intent.getStringExtra(EXTRA_DATE);
        String time = intent.getStringExtra(EXTRA_TIME);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String alertType = intent.getStringExtra(EXTRA_ALERT_TYPE);

        createChannelIfNeeded(context);
        showReminderNotification(context, reminderId, title, description, date, time, location, alertType);

        if ("RHYTHM".equalsIgnoreCase(alertType)) {
            playMelodyInBackground();
        }

        new ReminderDatabaseHelper(context).markReminderAsNotified(reminderId);
    }

    private void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AssignmentConfig.getNotificationChannelId(),
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Time-based reminders");
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showReminderNotification(
            Context context,
            int reminderId,
            String title,
            String description,
            String date,
            String time,
            String location,
            String alertType
    ) {
        String safeTitle = (title == null || title.trim().isEmpty()) ? "Reminder" : title;
        String assignmentMessage = AssignmentConfig.getNotificationMessage();
        String safeDate = date == null ? "" : date;
        String safeTime = time == null ? "" : time;
        String safeLocation = location == null ? "Not set" : location;

        String body = assignmentMessage + "\n" + safeDate + " " + safeTime + "\n" + "Location: " + safeLocation;
        if (description != null && !description.trim().isEmpty()) {
            body = description + "\n" + body;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AssignmentConfig.getNotificationChannelId())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Reminder: " + safeTitle)
                .setContentText(assignmentMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false);

        if ("RHYTHM".equalsIgnoreCase(alertType)) {
            builder.setSubText("Melody alert");
            builder.setVibrate(new long[]{0, 250, 150, 250, 150, 450});
        } else {
            builder.setVibrate(new long[]{0, 200, 100, 200});
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify(6000 + reminderId, builder.build());
    }

    private void playMelodyInBackground() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            try {
                int[] tones = new int[]{
                        ToneGenerator.TONE_CDMA_ABBR_ALERT,
                        ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                        ToneGenerator.TONE_CDMA_HIGH_L,
                        ToneGenerator.TONE_CDMA_MED_L,
                        ToneGenerator.TONE_CDMA_HIGH_L
                };
                for (int tone : tones) {
                    toneGenerator.startTone(tone, 220);
                    Thread.sleep(260);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                toneGenerator.release();
            }
        });
        executor.shutdown();
    }
}
