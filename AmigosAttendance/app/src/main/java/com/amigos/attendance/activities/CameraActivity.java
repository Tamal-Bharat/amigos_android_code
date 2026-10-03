package com.amigos.attendance.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amigos.attendance.R;
import com.amigos.attendance.ai.AiImageProcessingHelper;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.DialogUtility;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    TextView textViewCamera;
    private PreviewView previewView;

    ImageAnalysis imageAnalysis;

    private int counter = 0;
    int realFaceCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });

        initializeComponents();
        openCamera();
    }

    private void initializeComponents(){
        textViewCamera = findViewById(R.id.textViewCamera);
        previewView = findViewById(R.id.previewView);
    }

    private void openCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(CameraActivity.this);
        cameraProviderFuture.addListener(() -> {

            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                CameraSelector cameraSelector = ApplicationConstants.CAMERA_TYPE.equalsIgnoreCase("F") ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .build();

                imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        image -> {

                            Bitmap bitmap = imageProxyToBitmap(image);

                            //saveBitmapToGallery(bitmap);

                            System.out.println("CCCC--> " + "==================================");

                            counter++;

                            if (counter >= 30){
                                // Stop receiving new frames
                                imageAnalysis.clearAnalyzer();

                                runOnUiThread(() -> {
                                    textViewCamera.setText("Analysis Completed");
                                });

                                image.close();
                                return;
                            }

                            //detectRealFake(bitmap);
                            System.out.println("BBBB--> " + "Calling detectRealFake");
                            String returnValue = AiImageProcessingHelper.detectRealFake(CameraActivity.this, bitmap, textViewCamera);

                            if(returnValue.equalsIgnoreCase("REAL")){
                                realFaceCount++;
                            }

                            System.out.println("EEEE--> Return Value: " + returnValue);
                            System.out.println("EEEE--> realFaceCount: " + realFaceCount);

                            if(realFaceCount == 5){
                                imageAnalysis.clearAnalyzer();
                                image.close();
                                return;
                            }

                            System.out.println("CCCC--> " + "-------------------------------------");

                            image.close();
                        });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis);
            }
            catch(Exception e){
                new DialogUtility().getMeterialDialog(CameraActivity.this, "Exception", e.getMessage(), "error");
                System.out.println("AAAA--> " + "Exception: " + e.getMessage());
            }

        }, ContextCompat.getMainExecutor(CameraActivity.this));
    }

    private Bitmap imageProxyToBitmap(ImageProxy image){

        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth(),
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        bitmap.copyPixelsFromBuffer(buffer);

        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        if (rotationDegrees != 0){
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);

            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );
        }

        return bitmap;
    }

    private void saveBitmapToGallery(Bitmap bitmap) {

        String fileName = "meter_" + System.currentTimeMillis() + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/WBSEDCL"
            );
            contentValues.put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
            );
        }

        ContentResolver resolver = getContentResolver();

        Uri imageUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        );

        if (imageUri != null) {
            try {
                OutputStream outputStream = resolver.openOutputStream(imageUri);

                if (outputStream != null) {
                    bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            95,
                            outputStream
                    );

                    outputStream.flush();
                    outputStream.close();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(
                            MediaStore.Images.Media.IS_PENDING,
                            0
                    );

                    resolver.update(
                            imageUri,
                            updateValues,
                            null,
                            null
                    );
                }

                System.out.println("DDDD--> " +  "Image saved: " + imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}