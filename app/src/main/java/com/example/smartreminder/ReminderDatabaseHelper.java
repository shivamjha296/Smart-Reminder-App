package com.example.smartreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ReminderDB_22";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "reminder_22";

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TIME = "time";
    public static final String COL_LOCATION = "location";
    public static final String COL_CATEGORY = "category";

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
                + COL_CATEGORY + " TEXT" + ")";
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
        values.put(COL_CATEGORY, category);
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
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
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
}
