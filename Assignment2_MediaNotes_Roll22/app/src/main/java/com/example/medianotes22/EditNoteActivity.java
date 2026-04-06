package com.example.medianotes22;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private NotesDatabaseHelper dbHelper;

    private int noteId;
    private String selectedMediaPath;

    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etExtraField;
    private TextView tvMediaInfo;
    private ImageView ivMediaPreview;

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
                    // Some providers cannot grant persistence; file copy still works.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        setTitle(R.string.edit_note_screen_title);

        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);
        if (noteId <= 0) {
            Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new NotesDatabaseHelper(this);

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etExtraField = findViewById(R.id.etEditExtraField);
        tvMediaInfo = findViewById(R.id.tvEditMediaInfo);
        ivMediaPreview = findViewById(R.id.ivEditMediaPreview);

        etExtraField.setHint(formatExtraFieldLabel());

        Button btnChangeMedia = findViewById(R.id.btnChangeMedia);
        Button btnUpdateNote = findViewById(R.id.btnUpdateNote);

        btnChangeMedia.setOnClickListener(v -> showMediaOptionsDialog());
        btnUpdateNote.setOnClickListener(v -> updateNote());

        loadNote();
    }

    private void loadNote() {
        Note note = dbHelper.getNoteById(noteId);
        if (note == null) {
            Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle.setText(note.getTitle());
        etDescription.setText(note.getDescription());
        etExtraField.setText(note.getExtraFieldValue());

        selectedMediaPath = note.getImagePath();
        updateMediaPreview(selectedMediaPath, null);
    }

    private void showMediaOptionsDialog() {
        CharSequence[] options = new CharSequence[]{"Capture Image", "Select Image / Video"};
        new AlertDialog.Builder(this)
                .setTitle("Change Media")
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

    private void updateNote() {
        String title = safeEditText(etTitle).trim();
        String description = safeEditText(etDescription).trim();
        String extraFieldValue = safeEditText(etExtraField).trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

        if (extraFieldValue.isEmpty()) {
            etExtraField.setError(formatExtraFieldLabel() + " is required");
            return;
        }

        if (selectedMediaPath == null || selectedMediaPath.trim().isEmpty()) {
            Toast.makeText(this, "Please capture/select an image or video", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedRows = dbHelper.updateNoteById(
                noteId,
                title,
                description,
                selectedMediaPath,
                extraFieldValue
        );

        if (updatedRows > 0) {
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show();
        }
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

    private void updateMediaPreview(String mediaPath, String mimeType) {
        if (mediaPath == null || mediaPath.trim().isEmpty()) {
            tvMediaInfo.setText(getString(R.string.media_not_selected));
            ivMediaPreview.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        File file = new File(mediaPath);
        if (!file.exists()) {
            tvMediaInfo.setText("Media: File missing");
            ivMediaPreview.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        tvMediaInfo.setText("Media: " + file.getName());

        if (isVideo(mimeType, mediaPath)) {
            ivMediaPreview.setImageResource(android.R.drawable.ic_media_play);
            return;
        }

        ivMediaPreview.setImageURI(Uri.fromFile(file));
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

    private String safeEditText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString();
    }
}
