package com.example.smartreminder;

public final class AssignmentConfig {

    private AssignmentConfig() {
        // Utility class.
    }

    public static final int ROLL_NO = 22;

    public static String getDatabaseName() {
        return "ReminderDB_" + ROLL_NO;
    }

    public static String getTableName() {
        return "reminder_" + ROLL_NO;
    }

    public static String getExtraFieldColumn() {
        int mod = ROLL_NO % 3;
        if (mod == 0) {
            return "priority";
        }
        if (mod == 1) {
            return "category";
        }
        return "status";
    }

    public static int getNotificationIntervalMinutes() {
        int mod = ROLL_NO % 4;
        if (mod == 0) {
            return 5;
        }
        if (mod == 1) {
            return 10;
        }
        if (mod == 2) {
            return 15;
        }
        return 20;
    }

    public static int getWorkManagerIntervalMinutes() {
        return Math.max(15, getNotificationIntervalMinutes());
    }

    public static String getNotificationMessage() {
        int lastDigit = Math.abs(ROLL_NO % 10);
        if (lastDigit <= 3) {
            return "Reminder: Check your scheduled task";
        }
        if (lastDigit <= 6) {
            return "Alert: You have a pending reminder";
        }
        return "Time to complete your reminder";
    }

    public static String getWorkerUniqueName() {
        return "reminder_check_worker_" + ROLL_NO;
    }

    public static String getNotificationChannelId() {
        return "reminder_channel_" + ROLL_NO;
    }
}
