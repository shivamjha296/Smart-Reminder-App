package com.example.medianotes22;

public final class AssignmentConfig {

    private AssignmentConfig() {
        // Utility class.
    }

    public static final int ROLL_NO = 22;

    public static String getDatabaseName() {
        return "NotesDB_" + ROLL_NO;
    }

    public static String getTableName() {
        return "notes_" + ROLL_NO;
    }

    public static String getExtraFieldColumn() {
        int mod = ROLL_NO % 4;
        if (mod == 0) {
            return "note_type";
        }
        if (mod == 1) {
            return "priority";
        }
        if (mod == 2) {
            return "reminder_flag";
        }
        return "category";
    }

    public static String getNotificationMessage() {
        int mod = ROLL_NO % 3;
        if (mod == 0) {
            return "Review your saved notes today";
        }
        if (mod == 1) {
            return "Time to read your notes";
        }
        return "Check your notes and stay prepared";
    }

    public static String getNotificationChannelId() {
        return "notes_channel_" + ROLL_NO;
    }

    public static String getWorkerUniqueName() {
        return "notes_worker_" + ROLL_NO;
    }

    public static int getWorkIntervalMinutes() {
        return 15;
    }
}
