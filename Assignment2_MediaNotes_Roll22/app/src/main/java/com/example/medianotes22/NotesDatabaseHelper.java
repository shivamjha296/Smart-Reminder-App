package com.example.medianotes22;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = AssignmentConfig.getDatabaseName();
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = AssignmentConfig.getTableName();

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IMAGE_PATH = "image_path";
    public static final String COL_DATE = "date";
    public static final String COL_EXTRA_FIELD = AssignmentConfig.getExtraFieldColumn();

    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_IMAGE_PATH + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_EXTRA_FIELD + " TEXT" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertNote(String title, String description, String imagePath, String date, String extraFieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE_PATH, imagePath);
        values.put(COL_DATE, date);
        values.put(COL_EXTRA_FIELD, extraFieldValue);
        return db.insert(TABLE_NAME, null, values);
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String extraFieldValue = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXTRA_FIELD));
                notes.add(new Note(id, title, description, imagePath, date, extraFieldValue));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return notes;
    }

    public int getNoteCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int updateNoteById(int noteId, String title, String description, String imagePath, String extraFieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE_PATH, imagePath);
        values.put(COL_EXTRA_FIELD, extraFieldValue);
        return db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(noteId)});
    }

    public Note getNoteById(int noteId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(noteId)},
                null,
                null,
                null
        );

        Note note = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
            String extraFieldValue = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXTRA_FIELD));
            note = new Note(id, title, description, imagePath, date, extraFieldValue);
        }
        cursor.close();
        return note;
    }

    public int deleteNoteById(int noteId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(noteId)});
    }
}
