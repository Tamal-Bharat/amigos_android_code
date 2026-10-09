package com.amigos.attendance.OkHttpHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amigos.attendance.activities.FaceRegistrationActivity;
import com.amigos.attendance.models.EmployeeModel;
import com.amigos.attendance.models.FaceRegRespModel;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.AspectRatioVideoView;
import com.amigos.attendance.utilities.DialogUtility;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class UploadFaceRecordingOkHttp {

    OkHttpClient okHttpClient;
    RequestBody requestFileBody;
    MultipartBody requestMultipartBody;
    Request httpRequest;

    ContentResolver resolver;
    InputStream inputStream;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void uploadFaceRecording(
            Context context,
            ProgressBar faceRegistrationProgressBar,
            AspectRatioVideoView faceReg_VideoView,
            Button startStopRegFace_btn,
            Button regFace_btn,
            EditText regEmpId_editText,
            EditText regEmpName_editText,
            String fileName,
            Uri videoUri,
            String empId,
            String empName
    ) {

        System.out.println("AAAA--> " + "Video URI: " + videoUri);

        try{
            resolver = context.getContentResolver();
            inputStream = resolver.openInputStream(videoUri);
        }
        catch(Exception e){
            new DialogUtility().getMeterialDialog(context, "Error!!", e.getMessage(), "error");
        }

        if (inputStream == null) {
            new DialogUtility().getMeterialDialog(context, "Error!!", "Unable to open video URI", "error");
            return;
        }

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(ApplicationConstants.OKHTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApplicationConstants.OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApplicationConstants.OKHTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .callTimeout(ApplicationConstants.OKHTTP_CALL_TIMEOUT, TimeUnit.SECONDS)
                .build();
        //requestFileBody = RequestBody.create(file, MediaType.parse("video/mp4"));

        requestFileBody = new RequestBody() {

            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse("video/mp4");
            }

            @Override
            public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead =
                        inputStream.read(buffer)) != -1) {

                    bufferedSink.write(buffer, 0, bytesRead);
                }

                inputStream.close();
            }
        };

        requestMultipartBody = new MultipartBody
                                    .Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("video", fileName, requestFileBody)
                                    .addFormDataPart("emp_id", empId)
                                    .addFormDataPart("emp_name", empName)
                                    .build();


        httpRequest = new Request
                        .Builder()
                        .url(ApplicationConstants.BASE_URL + ApplicationConstants.REGISTER_FACE_RECORD)
                        .post(requestMultipartBody)
                        .build();

        faceRegistrationProgressBar.setVisibility(View.VISIBLE);
        faceReg_VideoView.pause();
        startStopRegFace_btn.setEnabled(false);
        regFace_btn.setEnabled(false);
        regEmpId_editText.setEnabled(false);
        regEmpName_editText.setEnabled(false);

        okHttpClient.newCall(httpRequest).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("AAAA--> " + "Video File upload failed: " + e.getMessage());
                faceRegistrationProgressBar.setVisibility(View.GONE);
                new DialogUtility().getMeterialDialog(context, "Error!", "Video File upload failed", "error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                mainHandler.post(() -> {
                    faceRegistrationProgressBar.setVisibility(View.GONE);
                    startStopRegFace_btn.setEnabled(true);
                    regFace_btn.setEnabled(true);
                    regEmpId_editText.setEnabled(true);
                    regEmpName_editText.setEnabled(true);
                });

                if(response.body() != null){
                    String result = response.body().string();
                    System.out.println("AAAA--> " + "Response body: " + result);

                    Gson gson = new Gson();
                    FaceRegRespModel faceRegRespModel = gson.fromJson(result, FaceRegRespModel.class);

                    System.out.println("AAAA--> " + faceRegRespModel);

                    if(faceRegRespModel.getCode() == 200){
                        if(faceRegRespModel.getMessage().getOpCode().equalsIgnoreCase("S")){
                            mainHandler.post(() ->{
                                new DialogUtility().getMeterialDialog(context, "Success!", faceRegRespModel.getMessage().getOpMessage(), "info");
                            });
                        }
                        else{
                            mainHandler.post(() ->{
                                new DialogUtility().getMeterialDialog(context, "Error!", faceRegRespModel.getMessage().getOpMessage(), "error");
                            });
                        }
                    }
                    else{
                        mainHandler.post(() ->{
                            new DialogUtility().getMeterialDialog(context, "Error!", faceRegRespModel.getMessage().getOpMessage(), "error");
                        });
                    }
                }
                else{
                    mainHandler.post(() ->{
                        new DialogUtility().getMeterialDialog(context, "Error!", "Video File upload failed", "error");
                    });
                }

            }
        });

    }

    public void doAttendance(Context context, TextView textViewCamera, Bitmap bitmap){

        try{

            if(bitmap == null){
                mainHandler.post(() ->{
                    new DialogUtility().getMeterialDialog(context, "Error!", "No Face Image Found", "error");
                });
            }
            else{

                OkHttpClient okHttpClient = new OkHttpClient
                                                .Builder()
                                                .connectTimeout(ApplicationConstants.OKHTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                                                .writeTimeout(ApplicationConstants.OKHTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                                                .readTimeout(ApplicationConstants.OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                                                .callTimeout(ApplicationConstants.OKHTTP_CALL_TIMEOUT, TimeUnit.SECONDS)
                                                .build();

                // Convert Bitmap to byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Create image request body
                RequestBody imageRequestBody = RequestBody.create(
                        imageBytes,
                        MediaType.parse("image/jpeg")
                );

                // Create multipart request
                MultipartBody requestBody = new MultipartBody
                        .Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "image",
                                "face_frame.jpg",
                                imageRequestBody
                        )
                        .build();


                // Create POST request
                Request request = new Request
                        .Builder()
                        .url(ApplicationConstants.BASE_URL + ApplicationConstants.ATTENDANCE_FACE_RECORD)
                        .post(requestBody)
                        .build();

                // Execute request asynchronously
                okHttpClient.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        System.out.println("AAAA--> UPLOAD ERROR: " + e.getMessage());
                        ((Activity) context).runOnUiThread(() -> {
                            textViewCamera.setTextColor(Color.parseColor("#ff0000"));
                            textViewCamera.setText("Connectivity Error!!.");
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response)
                            throws IOException {

                        try (Response res = response) {
                            if (res.body() != null) {
                                String result = res.body().string();

                                System.out.println("AAAA--> UPLOAD RESPONSE: " + result);

                                Gson gson = new Gson();
                                FaceRegRespModel faceRegRespModel = gson.fromJson(result, FaceRegRespModel.class);
                                //System.out.println("AAAA--> Status Code: " + faceRegRespModel.getMessage().getOpCode());

                                if(faceRegRespModel.getMessage().getOpCode().equalsIgnoreCase("S")){

                                    String employeeJson = faceRegRespModel.getMessage().getOpMessage().replace("'", "\"");
                                    EmployeeModel employeeModel  = new Gson().fromJson(employeeJson, EmployeeModel.class);

                                    ((Activity) context).runOnUiThread(() -> {
                                        textViewCamera.setTextColor(Color.parseColor("#0000ff"));
                                        textViewCamera.setText("Attendanec Successful" + "\n" + employeeModel.getEmp_id() + "\n" + employeeModel.getEmp_name());
                                    });
                                }
                                else{
                                    ((Activity) context).runOnUiThread(() -> {
                                        textViewCamera.setTextColor(Color.parseColor("#ff0000"));
                                        textViewCamera.setText(faceRegRespModel.getMessage().getOpMessage());
                                    });
                                }

                            }
                        }
                        catch(Exception e){
                            mainHandler.post(() ->{
                                new DialogUtility().getMeterialDialog(context, "Error!", e.getMessage(), "error");
                            });
                        }
                    }
                });

            }
        }
        catch(Exception e){
            mainHandler.post(() ->{
                new DialogUtility().getMeterialDialog(context, "Error!", e.getMessage(), "error");
            });
        }



    }
}
