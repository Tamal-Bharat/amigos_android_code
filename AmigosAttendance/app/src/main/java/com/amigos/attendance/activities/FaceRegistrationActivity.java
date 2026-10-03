package com.amigos.attendance.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.amigos.attendance.OkHttpHelper.UploadFaceRecordingOkHttp;
import com.amigos.attendance.R;
import com.amigos.attendance.fragments.RegisterFaceCameraFragment;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.AspectRatioVideoView;
import com.amigos.attendance.utilities.DialogUtility;
import com.amigos.attendance.utilities.MobilePermissionHelper;
import com.amigos.attendance.utilities.NetworkUtils;
import com.amigos.attendance.utilities.SqliteDatabaseHelper;

import java.io.File;
import java.util.ArrayList;

public class FaceRegistrationActivity extends AppCompatActivity {

    Button startStopRegFace_btn;
    Button regFace_btn;
    EditText regEmpId_editText;
    EditText regEmpName_editText;
    TextView videoTip_textView;

    //VideoView faceReg_VideoView;
    AspectRatioVideoView faceReg_VideoView;

    ProgressBar faceRegistrationProgressBar;

    private Uri videoUri;
    private ActivityResultLauncher<Intent> videoCaptureLauncher;
    private ActivityResultLauncher<IntentSenderRequest> deleteLauncher;

    private boolean isRecordingCompleted = false;
    private String faceRecordingName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_registration);
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

        startStopRegFace_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MobilePermissionHelper.isCameraPermissionGranted(FaceRegistrationActivity.this)) {
                    // Permission is already granted
                    System.out.println("AAAA--> " + "Already Camera Permission Granted");

                    //Open the Camera to register the attendance
                    try{
                        launchCamera();
                    }
                    catch(Exception e){
                        System.out.println("AAAA--> " + e.getMessage());
                        new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Error!", e.getMessage(), "error");
                    }
                }
                else {
                    // Permission is not granted
                    System.out.println("AAAA--> " + "Request for Camera Permission");
                    MobilePermissionHelper.requestCameraPermission(FaceRegistrationActivity.this);
                }

            }
        });

        regFace_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NetworkUtils.isInternetAvailable(FaceRegistrationActivity.this)){
                    registerFace();
                }
                else{
                    new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Error!!", "Please make available the internet", "error");
                }

            }
        });

    }

    private void initializeComponents(){

        startStopRegFace_btn = findViewById(R.id.startStopRegFace_btn);
        regFace_btn = findViewById(R.id.regFace_btn);
        regEmpId_editText = findViewById(R.id.regEmpId_editText);
        regEmpName_editText = findViewById(R.id.regEmpName_editText);
        videoTip_textView = findViewById(R.id.videoTip_textView);
        faceRegistrationProgressBar = findViewById(R.id.faceRegistrationProgressBar);

        faceReg_VideoView = findViewById(R.id.faceReg_VideoView);

        /*
        // Initialize ExoPlayer
        exoPlayer = new ExoPlayer.Builder(FaceRegistrationActivity.this).build();

        // Attach player to PlayerView
        faceReg_VideoView.setPlayer(exoPlayer);
        */

        videoCaptureLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);

                            if (videoUri == null) {
                                System.out.println("AAAA--> " + "VIDEO URI NULL: " + videoUri);
                                new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Alert!!", "Video URL is null", "error");
                                return;
                            }

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Video.Media.IS_PENDING, 0);

                            if (result.getResultCode() == Activity.RESULT_OK){

                                getContentResolver().update(
                                        videoUri,
                                        values,
                                        null,
                                        null
                                );

                                isRecordingCompleted = true;

                                System.out.println("AAAA--> " + "VIDEO SAVED: " + videoUri);
                                new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Success!!", "Face Video Recording Saved", "info");


                                faceReg_VideoView.setVideoURI(videoUri);
                                MediaController mediaController = new MediaController(FaceRegistrationActivity.this);
                                mediaController.setAnchorView(faceReg_VideoView);
                                faceReg_VideoView.setMediaController(mediaController);

                                faceReg_VideoView.setOnPreparedListener(mp -> {

                                    int videoWidth = mp.getVideoWidth();
                                    int videoHeight = mp.getVideoHeight();

                                    faceReg_VideoView.setVideoDimensions(
                                            videoWidth,
                                            videoHeight
                                    );

                                    mp.setLooping(false);

                                    // Start playback after the video is prepared
                                    faceReg_VideoView.start();

                                    // Request layout refresh
                                    faceReg_VideoView.requestLayout();
                                });

                                startStopRegFace_btn.setText("Record Again");
                                videoTip_textView.setText("Tap on the video to play");

                            }
                            else{
                                // Recording cancelled/failed.
                                // Delete the empty MediaStore entry.
                                getContentResolver().delete(
                                        videoUri,
                                        null,
                                        null
                                );

                                System.out.println("AAAA--> " + "Video recording cancelled");
                                new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Alert!!", "Video recording cancelled", "error");
                            }

                            //videoUri = null;

                        }
                );

    }

    /*
    @Override
    protected void onStop() {
        super.onStop();

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
    */

    private void launchCamera(){

        faceRecordingName = System.currentTimeMillis() + ApplicationConstants.VIDEO_FILE_NAME;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, faceRecordingName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Amigos/");
        //values.put(MediaStore.Video.Media.IS_PENDING, 1);

        videoUri = getContentResolver().insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values
        );

        if (videoUri != null){

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);

            intent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            intent.setClipData(
                    ClipData.newRawUri(
                            "VideoOutput",
                            videoUri
                    )
            );

            videoCaptureLauncher.launch(intent);
            overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);
        }
        else{
            new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Error!!", "Unable to create video file", "error");
        }

    }

    private void registerFace(){

        String empId = regEmpId_editText.getText().toString().trim();
        String empName = regEmpName_editText.getText().toString().trim();

        if(isRecordingCompleted){

            if(empId.isEmpty()){
                regEmpId_editText.setError("Enter Employee ID");
                return;
            }

            if(empName.isEmpty()){
                regEmpName_editText.setError("Enter Employee Name");
                return;
            }

            System.out.println("AAAA--> " + "All OK");

            File file;

            try{
                //file = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "/Amigos/" + faceRecordingName);
                new UploadFaceRecordingOkHttp().uploadFaceRecording(
                        FaceRegistrationActivity.this,
                        faceRegistrationProgressBar,
                        faceReg_VideoView,
                        startStopRegFace_btn,
                        regFace_btn,
                        regEmpId_editText,
                        regEmpName_editText,
                        faceRecordingName,
                        videoUri,
                        empId,
                        empName
                );
            }
            catch(Exception e){
                new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Error!!", e.getMessage(), "error");
            }

        }
        else{
            System.out.println("AAAA--> " + "No Recording Captured!!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ApplicationConstants.CAMERA_PERMISSION_REQUEST_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                System.out.println("AAAA--> " + "Camera permission granted");
                //new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Information!!", "Camera permission granted", "info");
                launchCamera();
            }
            else{
                System.out.println("AAAA--> " + "Camera permission denied");
                new DialogUtility().getMeterialDialog(FaceRegistrationActivity.this, "Alert!!", "Camera permission denied", "warning");
                SqliteDatabaseHelper databaseHelper = new SqliteDatabaseHelper(FaceRegistrationActivity.this);
                boolean isOpenSettings = databaseHelper.recordPermissionDeniedCount(databaseHelper, "camera_permission");

                if(isOpenSettings){

                    new DialogUtility()
                            .getAlertDialog(FaceRegistrationActivity.this,
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
}