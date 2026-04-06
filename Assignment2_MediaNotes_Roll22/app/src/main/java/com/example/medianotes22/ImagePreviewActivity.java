package com.example.medianotes22;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ImagePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "extra_image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ImageView ivPreview = findViewById(R.id.ivPreview);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath == null || imagePath.trim().isEmpty()) {
            Toast.makeText(this, "Image not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(this, "Image file missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivPreview.setImageURI(Uri.fromFile(imageFile));
        ivPreview.setOnClickListener(v -> finish());
    }
}
