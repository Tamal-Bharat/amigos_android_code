package com.amigos.attendance.fragments;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amigos.attendance.R;
import com.amigos.attendance.activities.CameraActivity;
import com.amigos.attendance.activities.HomeActivity;
import com.amigos.attendance.ai.AiImageProcessingHelper;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.DialogUtility;
import com.amigos.attendance.utilities.MobilePermissionHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;


public class RegisterFaceCameraFragment extends Fragment {

    private PreviewView regFacePreviewView;
    private ImageCapture regFaceImageCapture;

    public RegisterFaceCameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_face_camera, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        regFacePreviewView = view.findViewById(R.id.regFacePreviewView);
        startCamera();
    }



    private void startCamera(){

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(regFacePreviewView.getSurfaceProvider());
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

                            image.close();
                        });

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            }
            catch (Exception e) {
                new DialogUtility().getMeterialDialog(requireActivity(), "Exception", e.getMessage(), "error");
                //e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));

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
}