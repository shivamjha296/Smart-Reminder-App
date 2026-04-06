package com.example.medianotes22;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class NoteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private NotesDatabaseHelper dbHelper;

    private int noteId;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDate;
    private TextView tvExtraField;
    private TextView tvMediaType;
    private ImageView ivMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        setTitle(R.string.note_detail_title);

        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);
        if (noteId <= 0) {
            Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new NotesDatabaseHelper(this);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvDate = findViewById(R.id.tvDetailDate);
        tvExtraField = findViewById(R.id.tvDetailExtraField);
        tvMediaType = findViewById(R.id.tvDetailMediaType);
        ivMedia = findViewById(R.id.ivDetailMedia);

        Button btnEdit = findViewById(R.id.btnDetailEdit);
        Button btnDelete = findViewById(R.id.btnDetailDelete);

        btnEdit.setOnClickListener(v -> openEditScreen());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNote();
    }

    private void loadNote() {
        Note note = dbHelper.getNoteById(noteId);
        if (note == null) {
            Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(note.getTitle());
        tvDescription.setText(getString(R.string.label_description) + " " + safeText(note.getDescription()));
        tvDate.setText(getString(R.string.label_date) + " " + safeText(note.getDate()));
        tvExtraField.setText(getExtraFieldLabel() + " " + safeText(note.getExtraFieldValue()));

        bindMedia(note);
    }

    private void bindMedia(Note note) {
        String mediaPath = note.getImagePath();
        ivMedia.setOnClickListener(null);

        if (mediaPath == null || mediaPath.trim().isEmpty()) {
            tvMediaType.setText(getString(R.string.label_media) + " Not available");
            ivMedia.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        File mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            tvMediaType.setText(getString(R.string.label_media) + " File missing");
            ivMedia.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        if (note.isVideo()) {
            tvMediaType.setText(getString(R.string.label_media) + " Video attached");
            ivMedia.setImageResource(android.R.drawable.ic_media_play);
        } else {
            tvMediaType.setText(getString(R.string.label_media) + " Image attached");
            ivMedia.setImageURI(Uri.fromFile(mediaFile));
            ivMedia.setOnClickListener(v -> {
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_PATH, mediaPath);
                startActivity(intent);
            });
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_note_title)
                .setMessage(R.string.delete_note_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    int deletedRows = dbHelper.deleteNoteById(noteId);
                    if (deletedRows > 0) {
                        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, noteId);
        startActivity(intent);
    }

    private String getExtraFieldLabel() {
        return formatExtraFieldLabelOnly() + ":";
    }

    private String formatExtraFieldLabelOnly() {
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

    private String safeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value;
    }
}
