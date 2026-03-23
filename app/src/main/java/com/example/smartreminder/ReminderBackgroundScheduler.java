package com.example.smartreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class ReminderBackgroundScheduler {

    private static final int BACKGROUND_CHECK_REQUEST_CODE = 9222;

    private ReminderBackgroundScheduler() {
        // Utility class.
    }

    public static void schedule(Context context) {
        int intervalMinutes = AssignmentConfig.getNotificationIntervalMinutes();
        if (intervalMinutes < 15) {
            scheduleAlarmBasedChecks(context, intervalMinutes);
            WorkManager.getInstance(context).cancelUniqueWork(AssignmentConfig.getWorkerUniqueName());
            return;
        }

        cancelAlarmBasedChecks(context);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                intervalMinutes,
                TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AssignmentConfig.getWorkerUniqueName(),
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    private static void scheduleAlarmBasedChecks(Context context, int intervalMinutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = getBackgroundCheckPendingIntent(context);
        long intervalMillis = TimeUnit.MINUTES.toMillis(intervalMinutes);
        long firstTriggerAtMillis = System.currentTimeMillis() + intervalMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerAtMillis,
                    intervalMillis,
                    pendingIntent
            );
            return;
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTriggerAtMillis,
                intervalMillis,
                pendingIntent
        );
    }

    private static void cancelAlarmBasedChecks(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = getBackgroundCheckPendingIntent(context);
        alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent getBackgroundCheckPendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderCheckReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                BACKGROUND_CHECK_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
