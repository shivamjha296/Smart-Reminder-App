package com.example.medianotes22;

import java.util.Locale;

public class Note {
    private final int id;
    private final String title;
    private final String description;
    private final String imagePath;
    private final String date;
    private final String extraFieldValue;

    public Note(int id, String title, String description, String imagePath, String date, String extraFieldValue) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.date = date;
        this.extraFieldValue = extraFieldValue;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDate() {
        return date;
    }

    public String getExtraFieldValue() {
        return extraFieldValue;
    }

    public boolean isVideo() {
        if (imagePath == null) {
            return false;
        }

        String lowerPath = imagePath.toLowerCase(Locale.US);
        return lowerPath.endsWith(".mp4")
                || lowerPath.endsWith(".mkv")
                || lowerPath.endsWith(".3gp")
                || lowerPath.endsWith(".webm")
                || lowerPath.endsWith(".avi");
    }
}
