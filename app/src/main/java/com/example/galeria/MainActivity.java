package com.example.galeria;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.net.Uri;
import java.io.FileFilter;


public class MainActivity extends AppCompatActivity {
    static Uri photoURI;
    static String lastPhotoName;
    static String lastPhotoNameRecord="lastPhoto.txt";
    private ImageView imageView;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenGallery = findViewById(R.id.btnOpenGallery);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnHistory = findViewById(R.id.btnHistory);
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
                            //handleImageResult(result.getData().getExtras().get("data"));
                            setPic(photoURI);
                        }
                    }
                });

        getLastPhoto();

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, galeryHistory.class);
                startActivity(intent);
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
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
             photoURI = FileProvider.getUriForFile(this,
                    "com.example.galeria.fileprovider",
                    photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            cameraLauncher.launch(cameraIntent);

        }
    }

    private void handleImageResult(Object data) {
        if (data instanceof Uri) {
            // Galería
            Uri imageUri = (Uri) data;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                saveImageToInternalStorage(bitmap); // Guardar la imagen en almacenamiento interno
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Obtener el directorio de almacenamiento interno de la aplicación
            File internalStorageDir = getFilesDir();

            // Crear un nombre de archivo único (puedes mejorar esto según tus necesidades)
            String fileName = "pepe.jpg";

            // Crear un nuevo archivo en el directorio de almacenamiento interno
            File imageFile = new File(internalStorageDir, fileName);

            // Crear un flujo de salida para escribir la imagen en el archivo
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // Comprimir y escribir la imagen en el archivo
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            // Cerrar el flujo de salida
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        lastPhotoName=image.getName();
        return image;
    }


    private void setPic(Uri photoURI) {
        imageView.setImageURI(photoURI);
    }
    private void getLastPhoto() {
        // Obtener el directorio de imágenes externo
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Obtener la lista de archivos en el directorio de imágenes
        File[] files = picturesDir.listFiles();

        // Verificar si hay archivos y ordenarlos por fecha de modificación
        if (files != null && files.length > 0) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return Long.compare(file2.lastModified(), file1.lastModified());
                }
            });

            Uri uri = Uri.fromFile( files[0]);
            imageView.setImageURI(uri);
        } else {
            System.out.println("mal");
        }
    }

}
