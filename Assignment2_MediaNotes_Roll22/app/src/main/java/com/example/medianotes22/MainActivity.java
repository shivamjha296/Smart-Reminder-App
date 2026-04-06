package com.example.medianotes22;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private static final long SHAKE_COOLDOWN_MS = 1200L;

    private EditText etTitle;
    private EditText etDescription;
    private EditText etReminderFlag;
    private TextView tvSelectedMedia;
    private ImageView ivSelectedPreview;
    private NotesDatabaseHelper dbHelper;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTimestamp;

    private String selectedMediaPath;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCapturedPreview();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Void> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap == null) {
                    return;
                }

                String savedPath = saveBitmapToInternalStorage(bitmap);
                if (savedPath == null) {
                    Toast.makeText(this, "Failed to save captured image", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedMediaPath = savedPath;
                updateMediaPreview(savedPath, "image/jpeg");
            });

    private final ActivityResultLauncher<String[]> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }

                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                    // Grant persistence can fail for some providers; copy still works.
                }

                String mimeType = getContentResolver().getType(uri);
                String copiedPath = copyMediaToInternalStorage(uri, mimeType);
                if (copiedPath == null) {
                    Toast.makeText(this, "Unable to import selected media", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedMediaPath = copiedPath;
                updateMediaPreview(copiedPath, mimeType);
            });

    private final SensorEventListener shakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.values.length < 3) {
                return;
            }

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce < SHAKE_THRESHOLD_GRAVITY) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - lastShakeTimestamp < SHAKE_COOLDOWN_MS) {
                return;
            }

            lastShakeTimestamp = now;
            Toast.makeText(MainActivity.this, "Device motion detected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No-op.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NotesDatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etReminderFlag = findViewById(R.id.etReminderFlag);
        tvSelectedMedia = findViewById(R.id.tvSelectedMedia);
        ivSelectedPreview = findViewById(R.id.ivSelectedPreview);

        etReminderFlag.setHint(formatExtraFieldLabel());

        Button btnCaptureSelectMedia = findViewById(R.id.btnCaptureSelectMedia);
        Button btnSaveNote = findViewById(R.id.btnSaveNote);
        Button btnViewNotes = findViewById(R.id.btnViewNotes);

        btnCaptureSelectMedia.setOnClickListener(v -> showMediaOptionsDialog());
        btnSaveNote.setOnClickListener(v -> saveNote());
        btnViewNotes.setOnClickListener(v -> startActivity(new Intent(this, ViewNotesActivity.class)));

        requestNotificationPermissionIfNeeded();
        NotesBackgroundScheduler.schedule(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMediaOptionsDialog() {
        CharSequence[] options = new CharSequence[]{"Capture Image", "Select Image / Video"};
        new AlertDialog.Builder(this)
                .setTitle("Attach Media")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCameraCapture();
                    } else {
                        pickMediaLauncher.launch(new String[]{"image/*", "video/*"});
                    }
                })
                .show();
    }

    private void launchCameraCapture() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCapturedPreview();
            return;
        }

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void launchCapturedPreview() {
        captureImageLauncher.launch(null);
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        File mediaDirectory = new File(getFilesDir(), "note_media");
        if (!mediaDirectory.exists() && !mediaDirectory.mkdirs()) {
            return null;
        }

        File outputFile = new File(mediaDirectory, "capture_" + System.currentTimeMillis() + ".jpg");
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            if (!compressed) {
                return null;
            }
            outputStream.flush();
            return outputFile.getAbsolutePath();
        } catch (IOException ex) {
            return null;
        }
    }

    private String copyMediaToInternalStorage(Uri sourceUri, String mimeType) {
        File mediaDirectory = new File(getFilesDir(), "note_media");
        if (!mediaDirectory.exists() && !mediaDirectory.mkdirs()) {
            return null;
        }

        String extension = resolveExtension(mimeType);
        File outputFile = new File(mediaDirectory, "media_" + System.currentTimeMillis() + extension);

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return outputFile.getAbsolutePath();
        } catch (IOException ex) {
            return null;
        }
    }

    private String resolveExtension(String mimeType) {
        if (mimeType == null) {
            return ".bin";
        }

        if (mimeType.startsWith("image/")) {
            return ".jpg";
        }

        if (mimeType.startsWith("video/")) {
            return ".mp4";
        }

        return ".bin";
    }

    private void updateMediaPreview(String mediaPath, String mimeType) {
        File file = new File(mediaPath);
        tvSelectedMedia.setText("Media: " + file.getName());

        if (isVideo(mimeType, mediaPath)) {
            ivSelectedPreview.setVisibility(ImageView.GONE);
            ivSelectedPreview.setImageDrawable(null);
            Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show();
            return;
        }

        ivSelectedPreview.setVisibility(ImageView.VISIBLE);
        ivSelectedPreview.setImageURI(Uri.fromFile(file));
    }

    private boolean isVideo(String mimeType, String mediaPath) {
        if (mimeType != null && mimeType.startsWith("video/")) {
            return true;
        }

        String lowerPath = mediaPath.toLowerCase(Locale.US);
        return lowerPath.endsWith(".mp4")
                || lowerPath.endsWith(".mkv")
                || lowerPath.endsWith(".3gp")
                || lowerPath.endsWith(".webm")
                || lowerPath.endsWith(".avi");
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String extraFieldValue = etReminderFlag.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

        if (selectedMediaPath == null || selectedMediaPath.trim().isEmpty()) {
            Toast.makeText(this, "Please capture/select an image or video", Toast.LENGTH_SHORT).show();
            return;
        }

        if (extraFieldValue.isEmpty()) {
            etReminderFlag.setError(formatExtraFieldLabel() + " is required");
            return;
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        long rowId = dbHelper.insertNote(
                title,
                description,
                selectedMediaPath,
                createdAt,
                extraFieldValue
        );

        if (rowId <= 0) {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        etTitle.setText("");
        etDescription.setText("");
        etReminderFlag.setText("");
        selectedMediaPath = null;
        tvSelectedMedia.setText(R.string.media_not_selected);
        ivSelectedPreview.setVisibility(ImageView.GONE);
        ivSelectedPreview.setImageDrawable(null);
    }

    private String formatExtraFieldLabel() {
        String[] words = AssignmentConfig.getExtraFieldColumn().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.toString();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3010);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3010) {
            // No-op, app can continue without notifications.
        }
    }
}
