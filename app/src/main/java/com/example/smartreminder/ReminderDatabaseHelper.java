package com.example.smartreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = AssignmentConfig.getDatabaseName();
    public static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME = AssignmentConfig.getTableName();

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_LOCATION = "location";
    public static final String COL_EXTRA_FIELD = AssignmentConfig.getExtraFieldColumn();
    public static final String COL_ALERT_TYPE = "alert_type";
    public static final String COL_IS_NOTIFIED = "is_notified";

    public ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_TIME + " TEXT, "
                + COL_LOCATION + " TEXT, "
                + COL_EXTRA_FIELD + " TEXT, "
                + COL_ALERT_TYPE + " TEXT DEFAULT 'NOTIFICATION', "
                + COL_IS_NOTIFIED + " INTEGER DEFAULT 0" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertReminder(String title, String description, String date, String time, String location, String category, String alertType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DATE, date);
        values.put(COL_TIME, time);
        values.put(COL_LOCATION, location);
        values.put(COL_EXTRA_FIELD, category);
        values.put(COL_ALERT_TYPE, alertType);
        values.put(COL_IS_NOTIFIED, 0);
        return db.insert(TABLE_NAME, null, values);
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXTRA_FIELD));
                String alertType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_TYPE));
                reminders.add(new Reminder(id, title, description, date, time, location, category, alertType));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return reminders;
    }

    public int getReminderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int deleteReminderById(int reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(reminderId)});
    }

    public List<Reminder> getDueReminders(long nowMillis) {
        List<Reminder> dueReminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COL_IS_NOTIFIED + " = 0",
                null,
                null,
                null,
                COL_ID + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String timeText = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXTRA_FIELD));
                String alertType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_TYPE));

                Long scheduledAt = parseDateTimeToMillis(date, timeText);
                if (scheduledAt == null) {
                    continue;
                }

                if (scheduledAt <= nowMillis) {
                    dueReminders.add(new Reminder(id, title, description, date, timeText, location, category, alertType));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dueReminders;
    }

    public void markReminderAsNotified(int reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_NOTIFIED, 1);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(reminderId)});
    }

    private Long parseDateTimeToMillis(String dateText, String timeText) {
        if (dateText == null || timeText == null) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setLenient(false);
            Date parsed = formatter.parse(dateText + " " + timeText);
            return parsed == null ? null : parsed.getTime();
        } catch (ParseException ex) {
            return null;
        }
    }
}
