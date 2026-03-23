package com.example.smartreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

public final class ReminderAlarmScheduler {

    private ReminderAlarmScheduler() {
        // Utility class.
    }

    public static void scheduleReminder(Context context, Reminder reminder) {
        Long triggerAtMillis = reminder.getScheduledAtMillis();
        if (triggerAtMillis == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (triggerAtMillis <= now) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildReminderPendingIntent(context, reminder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            return;
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildReminderPendingIntent(context, reminderId);
        alarmManager.cancel(pendingIntent);
    }

    public static void rescheduleFutureReminders(Context context) {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(context);
        List<Reminder> reminders = dbHelper.getFutureUnnotifiedReminders(System.currentTimeMillis());
        for (Reminder reminder : reminders) {
            scheduleReminder(context, reminder);
        }
    }

    private static PendingIntent buildReminderPendingIntent(Context context, Reminder reminder) {
        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_TITLE, reminder.getTitle());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_DESCRIPTION, reminder.getDescription());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_DATE, reminder.getDate());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_TIME, reminder.getTime());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_LOCATION, reminder.getLocation());
        intent.putExtra(ReminderAlarmReceiver.EXTRA_ALERT_TYPE, reminder.getAlertType());

        return PendingIntent.getBroadcast(
                context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static PendingIntent buildReminderPendingIntent(Context context, int reminderId) {
        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
