package com.example.smartreminder;

public class Reminder {
    private final int id;
    private final String title;
    private final String description;
    private final String time;
    private final String location;
    private final String category;

    public Reminder(int id, String title, String description, String time, String location, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.time = time;
        this.location = location;
        this.category = category;
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

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }
}
