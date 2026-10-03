
class Constants():

    # Model file name
    DETECTOR_MODEL_PATH = "ai_models/face_detection_yunet_2026may.onnx"

    # constants regarding face detector model 
    CHECK_INTERVAL_SECONDS = 3
    CONFIDENCE_THRESHOLD = 0.65
    NMS_THRESHOLD = 0.3
    TOP_K = 5000

    # Face Video upload and Frame location folder names
    UPLOAD_VIDEO_FOLDER = "videos"
    FRAME_OUTPUT_DIR = "frames"

    # Save frame as image for similarity check
    IMAGE_FRAME_FOLDER = "image_frame"
    IMAGE_FRAME_NAME = "frame.jpg"

    #Log File 
    LOG_FIEL_FOLDER = "logs"

    # connection parameters regarding database
    DATABASE_HOST = "192.168.29.198"
    DATABASE_NAME = "postgres"
    DATABASE_PORT = 5432
    DATABASE_USER = "postgres"
    DATABASE_PASSWORD = "postgres"


    #Error Codes
    ERR_404 = "ERR_404"


    def __init__(self):
        pass
    
