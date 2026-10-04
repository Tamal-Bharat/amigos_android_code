package com.amigos.attendance.utilities;

public class ApplicationConstants {

    public static String YOLO_MODEL = "best.onnx";
    //public static String YOLO_MODEL = "yolov8s.onnx";

    public static int DELAY_LOAD_HOME_PAGE = 5000;
    public static int DELAY_DIALOG_DISAPPEAR = 3000;
    public static int DELAY_DIALOG_FADE_IN = 1000;
    public static int CAMERA_PERMISSION_REQUEST_CODE = 100;

    //okHttp Timeout parameters
    public static int OKHTTP_CONNECTION_TIMEOUT = 30;
    public static int OKHTTP_READ_TIMEOUT = 60;
    public static int OKHTTP_WRITE_TIMEOUT = 60;
    public static int OKHTTP_CALL_TIMEOUT = 120;

    public static String CAMERA_TYPE = "F";
    //public static String CAMERA_TYPE = "B";

    public static String VIDEO_FILE_NAME = ".mp4";

    public static String SQLITE_DATABASE_NAME = "attendance.db";
    public static int SQLITE_DATABASE_VERSION = 1;


    //Public Services
    public static String BASE_URL = "http://192.168.29.22:8000";
    public static String REGISTER_FACE_RECORD = "/attandance/register-face";
    public static String ATTENDANCE_FACE_RECORD = "/attandance/check-similarity";

    //Permission table
    public static String PERMISSION_DENIED_TABLE = "pdenied";
    public static String PERMISSION_DENIED_TABLE_ROW_SL_NO = "sl_no";
    public static String PERMISSION_DENIED_TABLE_ROW_PERMISSION_NAME = "p_name";
    public static String PERMISSION_DENIED_TABLE_ROW_PERMISSION_DENIED_COUNT = "p_d_count";
}
