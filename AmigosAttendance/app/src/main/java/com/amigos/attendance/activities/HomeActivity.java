package com.amigos.attendance.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amigos.attendance.R;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.DialogUtility;
import com.amigos.attendance.utilities.MobilePermissionHelper;
import com.amigos.attendance.utilities.SqliteDatabaseHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    ImageView img_register;
    ImageView img_record_attendance;

    private ActivityResultLauncher<Intent> cameraActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeActivity), (v, insets) -> {
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

        //startActivityLauncher();
        initializeComponents();

        img_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent faceRegistrationActivityIntent = new Intent(HomeActivity.this, FaceRegistrationActivity.class);
                startActivity(faceRegistrationActivityIntent);
                overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);
            }
        });

        img_record_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MobilePermissionHelper.isCameraPermissionGranted(HomeActivity.this)) {
                    // Permission is already granted
                    System.out.println("AAAA--> " + "Already Camera Permission Granted");

                    //Open the Camera to register the attendance
                    startCameraToRecognizeFace();
                } else {
                    // Permission is not granted
                    System.out.println("AAAA--> " + "Request for Camera Permission");
                    MobilePermissionHelper.requestCameraPermission(HomeActivity.this);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if (requestCode == ApplicationConstants.CAMERA_PERMISSION_REQUEST_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                System.out.println("AAAA--> " + "Camera permission granted");
                startCameraToRecognizeFace();
            }
            else{
                System.out.println("AAAA--> " + "Camera permission denied");
                SqliteDatabaseHelper databaseHelper = new SqliteDatabaseHelper(HomeActivity.this);
                boolean isOpenSettings = databaseHelper.recordPermissionDeniedCount(databaseHelper, "camera_permission");

                if(isOpenSettings){

                    new DialogUtility()
                            .getAlertDialog(HomeActivity.this,
                            "Information",
                            "Enable Camera Permission through App Settings.",
                            "info")
                            .setPositiveButton("Ok", (dialog, which)->{
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                                dialog.cancel();
                            })
                            .show();

                }

            }

        }
    }

    private void initializeComponents(){

        img_register = findViewById(R.id.img_register);
        img_record_attendance = findViewById(R.id.img_record_attendance);

        cameraActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK){

                    }
                    else if (result.getResultCode() == RESULT_CANCELED){

                    }

                });
    }

    private void startCameraToRecognizeFace(){
        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
        cameraActivityLauncher.launch(intent);
        overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);
    }

}