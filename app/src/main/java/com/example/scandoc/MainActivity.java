package com.example.scandoc;

import static android.provider.MediaStore.EXTRA_OUTPUT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.scandoc.databinding.ActivityMainBinding;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
            saveAsPdf();
        });
    }

    private void saveAsPdf() {
        File dir = new File(Environment.getDataDirectory(),"SaveImage");
        if (!dir.exists()){
            dir.mkdir();
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imgScanned.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        File file = new File(dir, System.currentTimeMillis()+".jpg");
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
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