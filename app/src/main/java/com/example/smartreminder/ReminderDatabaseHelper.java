package com.example.smartreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = AssignmentConfig.getDatabaseName();
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = AssignmentConfig.getTableName();

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TIME = "time";
    public static final String COL_LOCATION = "location";
    public static final String COL_EXTRA_FIELD = AssignmentConfig.getExtraFieldColumn();

    public ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_TIME + " TEXT, "
                + COL_LOCATION + " TEXT, "
                + COL_EXTRA_FIELD + " TEXT" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertReminder(String title, String description, String time, String location, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_TIME, time);
        values.put(COL_LOCATION, location);
        values.put(COL_EXTRA_FIELD, category);
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
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXTRA_FIELD));
                reminders.add(new Reminder(id, title, description, time, location, category));
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

    public int getDueReminderCount(int lookBackMinutes) {
        int dueCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_TIME}, null, null, null, null, null);

        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        if (cursor.moveToFirst()) {
            do {
                String timeText = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                Integer reminderMinutes = parseTimeToMinutes(timeText);
                if (reminderMinutes == null) {
                    continue;
                }

                int minutesSinceReminder = currentMinutes - reminderMinutes;
                if (minutesSinceReminder < 0) {
                    minutesSinceReminder += 24 * 60;
                }

                if (minutesSinceReminder >= 0 && minutesSinceReminder < lookBackMinutes) {
                    dueCount++;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dueCount;
    }

    private Integer parseTimeToMinutes(String timeText) {
        if (timeText == null || !timeText.matches("\\d{2}:\\d{2}")) {
            return null;
        }

        try {
            String[] parts = timeText.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
