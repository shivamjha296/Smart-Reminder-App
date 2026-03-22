# Smart Reminder App (Roll No 22)

This project is built for **Mobile App Development Lab - Assignment 1** using:
- Java (Android)
- Groovy Gradle scripts
- SQLite
- WorkManager
- GPS location via `LocationManager`

## Roll Number Personalization Applied
- Roll No: `22`
- Database name: `ReminderDB_22`
- Table name: `reminder_22`
- `22 % 3 = 1` -> Extra field: `category`
- Last digit `2` -> Notification message: `Reminder: Check your scheduled task`
- `22 % 4 = 2` -> Background interval: `15 minutes`

## Implemented Features
1. Add reminder form with:
   - Reminder Title
   - Reminder Description
   - Reminder Time
   - Category (personalized field)
   - GPS location capture button
2. SQLite storage with fields:
   - `id`, `title`, `description`, `time`, `location`, `category`
3. View reminders screen using `RecyclerView`
4. Background reminder checks with `WorkManager` every 15 minutes
5. Notification generation based on assignment rule

## Key Files
- `app/src/main/java/com/example/smartreminder/MainActivity.java`
- `app/src/main/java/com/example/smartreminder/ReminderDatabaseHelper.java`
- `app/src/main/java/com/example/smartreminder/ReminderWorker.java`
- `app/src/main/java/com/example/smartreminder/ViewRemindersActivity.java`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/activity_view_reminders.xml`

## How to Run
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run app on emulator or physical device.
4. Grant location and notification permissions when prompted.

## Notes
- `WorkManager` minimum periodic interval is 15 minutes, which exactly matches your required interval.
- GPS uses last known location; if null, move outdoors and try again.
