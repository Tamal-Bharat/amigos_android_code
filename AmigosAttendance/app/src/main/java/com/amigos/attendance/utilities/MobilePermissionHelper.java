package com.amigos.attendance.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MobilePermissionHelper {

    public static int CAMERA_PERMISSION_REQUEST_CODE = ApplicationConstants.CAMERA_PERMISSION_REQUEST_CODE;

    //Check whether Camera permission is granted
    public static boolean isCameraPermissionGranted(Activity activity) {

        return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    //Request permission.
    public static void requestCameraPermission(Activity activity) {

        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

}
