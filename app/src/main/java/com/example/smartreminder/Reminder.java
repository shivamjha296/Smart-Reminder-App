package com.example.smartreminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Reminder {
    private final int id;
    private final String title;
    private final String description;
    private final String date;
    private final String time;
    private final String location;
    private final String category;
    private final String alertType;

    public Reminder(int id, String title, String description, String date, String time, String location, String category, String alertType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.category = category;
        this.alertType = alertType;
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

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }

    public String getAlertType() {
        return alertType;
    }

    public Long getScheduledAtMillis() {
        if (date == null || time == null) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setLenient(false);
            Date parsed = formatter.parse(date + " " + time);
            return parsed == null ? null : parsed.getTime();
        } catch (ParseException ex) {
            return null;
        }
    }

    public boolean isExpired() {
        Long scheduledAtMillis = getScheduledAtMillis();
        return scheduledAtMillis != null && scheduledAtMillis < System.currentTimeMillis();
    }
}
