package com.amigos.attendance.OkHttpHelper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amigos.attendance.activities.FaceRegistrationActivity;
import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.DialogUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public void uploadFaceRecording(Context context, String fileName, Uri videoUri, String empId, String empName) {

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

        okHttpClient = new OkHttpClient();
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

        okHttpClient.newCall(httpRequest).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("AAAA--> " + "Video File upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.body() != null){
                    String result = response.body().string();
                    System.out.println("AAAA--> " + "Response body: " + result);
                }
                else{
                    System.out.println("AAAA--> " + "Response body null");
                }

            }
        });

    }
}
