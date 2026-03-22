package com.example.smartreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private EditText etTime;
    private EditText etCategory;
    private TextView tvLocation;
    private ReminderDatabaseHelper dbHelper;

    private String currentLocationText = "Not set";

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fine != null && fine) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ReminderDatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etTime = findViewById(R.id.etTime);
        etCategory = findViewById(R.id.etCategory);
        tvLocation = findViewById(R.id.tvLocation);

        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        Button btnAddReminder = findViewById(R.id.btnAddReminder);
        Button btnViewReminders = findViewById(R.id.btnViewReminders);

        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnGetLocation.setOnClickListener(v -> requestLocationPermissionAndFetch());
        btnAddReminder.setOnClickListener(v -> addReminder());
        btnViewReminders.setOnClickListener(v -> startActivity(new Intent(this, ViewRemindersActivity.class)));

        requestNotificationPermissionIfNeeded();
        scheduleReminderWorker();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    etTime.setText(formattedTime);
                    Toast.makeText(this, "Time set: " + formattedTime, Toast.LENGTH_SHORT).show();
                },
                currentHour,
                currentMinute,
                true
        );

        timePickerDialog.show();
    }

    private void addReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("Time is required");
            return;
        }

        if (category.isEmpty()) {
            etCategory.setError(AssignmentConfig.getExtraFieldColumn() + " is required");
            return;
        }

        long rowId = dbHelper.insertReminder(title, description, time, currentLocationText, category);
        if (rowId > 0) {
            Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etDescription.setText("");
            etTime.setText("");
            etCategory.setText("");
            currentLocationText = "Not set";
            tvLocation.setText(getString(R.string.location_placeholder));
        } else {
            Toast.makeText(this, "Failed to add reminder", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
            return;
        }

        permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(this, "Location service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            currentLocationText = String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", lat, lon);
            tvLocation.setText("Location: " + currentLocationText);
        } else {
            Toast.makeText(this, "Unable to fetch location. Try again outdoors.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleReminderWorker() {
        int intervalMinutes = AssignmentConfig.getWorkManagerIntervalMinutes();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReminderWorker.class, intervalMinutes, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AssignmentConfig.getWorkerUniqueName(),
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2001) {
            // No-op; app can still function without notifications.
        }
    }
}
