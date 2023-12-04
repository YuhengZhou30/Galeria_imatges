package com.example.galeria;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenGallery = findViewById(R.id.btnOpenGallery);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        imageView = findViewById(R.id.imageView);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            handleImageResult(result.getData().getData());
                        }
                    }
                });

        // Inicializar el ActivityResultLauncher para la cámara
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            handleImageResult(result.getData().getExtras().get("data"));
                        }
                    }
                });

        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void handleImageResult(Object data) {
        if (data instanceof Uri) {
            // Galería
            Uri imageUri = (Uri) data;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (data instanceof Bitmap) {
            // Cámara
            Bitmap bitmap = (Bitmap) data;
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}
