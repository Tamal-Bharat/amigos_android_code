package com.amigos.attendance.ai;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.TextView;

import com.amigos.attendance.utilities.ApplicationConstants;
import com.amigos.attendance.utilities.DialogUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.security.spec.ECField;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class AiImageProcessingHelper {

    private static TextView textView;
    private static  Activity activity;

    Context context;

    private static OrtEnvironment ortEnvironment;
    private static OrtSession ortSession;

    private static final String[] CLASS_NAMES = {
            "fake",
            "real"
    };

    public static String detectRealFake(Context contextCamera, Bitmap faceBitmap, TextView textViewCamera){

        String returnValue = "";

        activity = (Activity) contextCamera;
        textView = textViewCamera;

        initRealFakeModel(contextCamera);

        if (faceBitmap == null || ortSession == null) {
            //new DialogUtility().getMeterialDialog(contextCamera, "Error!", "Captured Bitmap Image is null", "error");
            returnValue =  "faceBitmap or ortSession null";
        }

        try{
            final int INPUT_SIZE = 640;

            // Resize face
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    faceBitmap,
                    INPUT_SIZE,
                    INPUT_SIZE,
                    true
            );

            // YOLO classification input:
            // [1, 3, 224, 224]
            float[] inputData = bitmapToFloatArray(resizedBitmap);

            long[] inputShape = {
                    1,
                    3,
                    INPUT_SIZE,
                    INPUT_SIZE
            };

            OnnxTensor inputTensor =
                    OnnxTensor.createTensor(
                            ortEnvironment,
                            FloatBuffer.wrap(inputData),
                            inputShape
                    );

            // Get input name
            String inputName =
                    ortSession.getInputNames()
                            .iterator()
                            .next();

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put(inputName, inputTensor);

            // Run inference
            OrtSession.Result results = ortSession.run(inputs);

            // Get output
            Object output = results.get(0).getValue();

            if (output instanceof float[][][]) {
                float[][][] outputData = (float[][][]) output;
                System.out.println("AAAA--> " +  "Output dimensions = " + outputData.length + " x " + outputData[0].length + " x " + outputData[0][0].length);
                returnValue =  processYoloOutput(contextCamera, outputData);
            }
            else {
                //new DialogUtility().getMeterialDialog(contextCamera, "Error!", "Unexpected output type: " + output.getClass(), "error");
                System.out.println("AAAA--> " + "Unexpected output type: " + output.getClass());
                returnValue =  "Unexpected output type";
            }

            /*
            float[] probabilities;

            if (output instanceof float[][]) {
                probabilities = ((float[][]) output)[0];
            }
            else if (output instanceof float[]) {
                probabilities = (float[]) output;
            }
            else {
                System.out.println("AAAA--> " + "REAL_FAKE " + "Unexpected output type: " + output.getClass());
                //new DialogUtility().getMeterialDialog(contextCamera, "Error!", "Unexpected output type: " + output.getClass(), "error");
                results.close();
                inputTensor.close();
                return "Unexpected output type";
            }

            // Find highest probability
            int bestClass = 0;
            float bestConfidence = probabilities[0];

            for (int i = 1; i < probabilities.length; i++) {

                if (probabilities[i] > bestConfidence) {

                    bestConfidence = probabilities[i];
                    bestClass = i;
                }
            }

            String className = CLASS_NAMES[bestClass];

            System.out.println("AAAA--> " + "REAL_FAKE" +  "Class = " + className + " Confidence = " + bestConfidence);

            if (className.equals("real")) {
                System.out.println("AAAA--> " + "REAL FACE : " );
            }
            else {
                System.out.println("AAAA--> " + "FAKE FACE : " );
            }
            */

            results.close();
            inputTensor.close();

        }
        catch(Exception e){
            //new DialogUtility().getMeterialDialog(contextCamera, "Exception", e.getMessage(), "error");
            System.out.println("AAAA--> " + "Exception: " + e.getMessage());
            returnValue =  e.getMessage();
        }

        return returnValue;

    }

    private static float[] bitmapToFloatArray(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float[] input = new float[3 * width * height];
        int[] pixels = new int[width * height];

        bitmap.getPixels(
                pixels,
                0,
                width,
                0,
                0,
                width,
                height
        );

        int channelSize = width * height;

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int pixel = pixels[y * width + x];

                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;

                int index = y * width + x;

                // CHW format
                input[index] = r;
                input[channelSize + index] = g;
                input[2 * channelSize + index] = b;
            }
        }

        return input;
    }

    private static String processYoloOutput(Context context, float[][][] output) {

        try {

            // Expected:
            // output shape = [1][6][8400]
            //
            // 6 =
            // x
            // y
            // width
            // height
            // fake confidence
            // real confidence
            //
            // 8400 = number of detection candidates

            if (output == null ||
                    output.length == 0 ||
                    output[0].length == 0 ||
                    output[0][0].length == 0) {

                System.out.println("AAAA--> " + "Empty YOLO output");
                //new DialogUtility().getMeterialDialog(context, "Exception", "Empty YOLO output", "error");

                return "Empty YOLO output";
            }

            int dimensions = output[0].length;
            int candidates = output[0][0].length;

            System.out.println("AAAA--> " + "YOLO dimensions = " + dimensions);
            System.out.println("AAAA--> " + "YOLO candidates = " + candidates);

            // We expect:
            // 4 box values + 2 class values = 6
            if (dimensions < 6) {
                System.out.println("AAAA--> " + "Unexpected YOLO output dimensions: " + dimensions);
                //new DialogUtility().getMeterialDialog(context, "Exception", "Unexpected YOLO output dimensions: " + dimensions, "error");
                return "Unexpected YOLO output dimensions";
            }

            float bestConfidence = 0.0f;
            int bestClass = -1;

            float bestX = 0;
            float bestY = 0;
            float bestW = 0;
            float bestH = 0;

            // Iterate through all YOLO candidates
            for (int i = 0; i < candidates; i++) {

                float x = output[0][0][i];
                float y = output[0][1][i];
                float w = output[0][2][i];
                float h = output[0][3][i];

                // Class 0 = fake
                float fakeConfidence = output[0][4][i];

                // Class 1 = real
                float realConfidence = output[0][5][i];

                // Find highest class confidence
                if (fakeConfidence > bestConfidence) {
                    bestConfidence = fakeConfidence;

                    bestClass = 0;

                    bestX = x;
                    bestY = y;
                    bestW = w;
                    bestH = h;
                }

                if (realConfidence > bestConfidence) {

                    bestConfidence = realConfidence;

                    bestClass = 1;

                    bestX = x;
                    bestY = y;
                    bestW = w;
                    bestH = h;
                }

            }

            // No valid detection
            System.out.println("AAAA--> " + "Best Class: " + bestClass);

            if (bestClass == -1) {
                System.out.println("AAAA--> " + "No face detected");

                activity.runOnUiThread(() -> {
                    textView.setText("NO FACE");
                });

                return "No face detected";
            }

            String className;

            if (bestClass == 0) {
                className = "FAKE";
            }
            else {
                className = "REAL";
            }

            System.out.println("AAAA--> " + "Detection = " + className + " Confidence = " + bestConfidence);
            System.out.println("AAAA--> " + "Box = " + bestX + ", " + bestY + ", " + bestW + ", " + bestH);

            // Confidence threshold
            if (bestConfidence < 0.50f) {

                activity.runOnUiThread(() -> {
                    textView.setText("UNCERTAIN");
                });

                return "UNCERTAIN";
            }

            final String resultText = className + " : " + String.format("%.1f%%", bestConfidence * 100);

            // Update Android UI
            activity.runOnUiThread(() -> {
                textView.setText(resultText);
            });

            return className;

        } catch (Exception e) {
            System.out.println("AAAA--> " + "YOLO output processing error: " + e.getMessage());
            return "YOLO output processing error";
        }
    }

    private static void initRealFakeModel(Context contextCamera) {

        try {

            ortEnvironment = OrtEnvironment.getEnvironment();

            byte[] modelBytes = getModelBytes(contextCamera, ApplicationConstants.YOLO_MODEL);

            ortSession = ortEnvironment.createSession(
                    modelBytes,
                    new OrtSession.SessionOptions()
            );

            System.out.println("AAAA--> " +  "Model loaded successfully");

            // Print model input information
            for (Map.Entry<String, NodeInfo> entry : ortSession.getInputInfo().entrySet()) {
                System.out.println("AAAA--> " +  "Input: " + entry.getKey() + " -> " + entry.getValue().getInfo());
            }

        } catch (Exception e) {
            System.out.println("AAAA--> " + "Error loading model" + e.getMessage());
            //new DialogUtility().getMeterialDialog(contextCamera, "Exception", e.getMessage(), "error");
        }
    }

    private static byte[] getModelBytes(Context contextCamera, String fileName) throws IOException {

        InputStream inputStream = contextCamera.getAssets().open(fileName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        inputStream.close();
        return buffer.toByteArray();
    }
}
