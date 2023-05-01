package com.example.scandoc;

import static android.provider.MediaStore.EXTRA_OUTPUT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.scandoc.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import team.clevel.documentscanner.ImageCropActivity;
import team.clevel.documentscanner.helpers.ScannerConstants;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static int CAMERA_ACCESS_CODE = 99, CAMERA_CODE=98, REQUEST_CROP=97;
    private Uri imageUri;
    private String imagePath;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        binding.btnScan.setOnClickListener(v -> {
            captureImage();
        });
        binding.btnSave.setOnClickListener(v -> {
            saveLocally();
        });
    }

    private void saveLocally() {
        Uri image;
        ContentResolver contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            image = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else {
            image = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,System.currentTimeMillis()+".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/*");
        Uri uri = contentResolver.insert(image, contentValues);
        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imgScanned.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void captureImage() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},CAMERA_ACCESS_CODE);
        }
        else {
            Intent clickPic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (clickPic.resolveActivity(getPackageManager()) != null){
                File photoFile = createImageFile();
                if (photoFile != null){
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName()+".provider", photoFile);
                    imageUri = fileUri;
                    clickPic.putExtra(EXTRA_OUTPUT, fileUri);
                    startActivityForResult(clickPic, CAMERA_CODE);
                }
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
                Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = null;
        try{
            imageFile = File.createTempFile(imageFileName,".jpg",storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        imagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CROP){
            if (resultCode == RESULT_OK){
                if (ScannerConstants.selectedImageBitmap !=null){
                    binding.imgScanned.setImageBitmap(ScannerConstants.selectedImageBitmap);
                }
            }
        }

        if (requestCode == CAMERA_CODE){
            if (resultCode == RESULT_OK){
                Uri contentUri = imageUri;
                try {
                    Bitmap bmpImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    Bitmap rotatedImage = getRotatedImage(bmpImage, imagePath);
                    createImage(rotatedImage, contentUri.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createImage(Bitmap rotatedImage, String path) {
        cropImage(rotatedImage,REQUEST_CROP);
    }

    private void cropImage(Bitmap rotatedImage, int requestCrop) {
        ScannerConstants.selectedImageBitmap = rotatedImage;
        startActivityForResult(new Intent(MainActivity.this, ImageCropActivity.class), requestCrop);
    }

    private Bitmap getRotatedImage(Bitmap bmpImage, String imagePath) {
        return bmpImage;
    }
}