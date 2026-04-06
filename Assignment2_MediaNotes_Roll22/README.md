# Assignment 2 - Media Notes App with Sensor and Notification (Roll No 22)

This is a separate Android project for Assignment 2.

## Roll Number Personalization
- Roll No: 22
- Database name: NotesDB_22
- Table name: notes_22
- 22 % 4 = 2 -> Extra field: reminder_flag
- 22 % 3 = 1 -> Notification message: Time to read your notes

## Implemented Features
1. Main screen with:
   - Note Title
   - Note Description
   - Extra field: Reminder Flag
   - Button: Capture Image / Select Image
   - Button: Save Note
   - Button: View Notes
2. SQLite database stores:
   - id, title, description, image_path, date, reminder_flag
3. Media support:
   - Capture image from camera
   - Select image/video from storage
   - Save media path with note
4. Notes listing:
   - RecyclerView based compact notes list (short text + side image)
   - Tap a list item to open full note detail screen
   - Image preview for image notes and video icon indicator for video notes
   - Edit/Delete actions available in detail screen
5. Background reminder:
   - WorkManager periodic worker (15 minutes)
   - Notification text: Time to read your notes
6. Accelerometer integration:
   - On shake, shows toast: Device motion detected

## Key Files
- app/src/main/java/com/example/medianotes22/MainActivity.java
- app/src/main/java/com/example/medianotes22/NotesDatabaseHelper.java
- app/src/main/java/com/example/medianotes22/NotesReminderWorker.java
- app/src/main/java/com/example/medianotes22/ViewNotesActivity.java
- app/src/main/java/com/example/medianotes22/AssignmentConfig.java

## Build
From this folder:
- Windows: gradlew.bat :app:assembleDebug
