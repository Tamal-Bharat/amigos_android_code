import cv2
import os
from utilities.Constants import Constants
from utilities.LogFile import LogFile

class AIModelHelper():
    
    AppConstants = Constants()
    AppLogFile = LogFile()
    
    def  getFaceDetector(self)-> cv2.FaceDetectorYN | None:    

        try:


            if not os.path.exists(self.AppConstants.DETECTOR_MODEL_PATH):                
                self.AppLogFile.writeLogs("ERROR: YuNet model not found:")           
                raise Exception("ERROR: YuNet model not found")

            else:
                # Create YuNet face detector
                detector = cv2.FaceDetectorYN.create(
                    self.AppConstants.DETECTOR_MODEL_PATH,
                    "",
                    (320, 320),
                    self.AppConstants.CONFIDENCE_THRESHOLD,
                    self.AppConstants.NMS_THRESHOLD,
                    self.AppConstants.TOP_K,
                    cv2.dnn.DNN_BACKEND_OPENCV,
                    cv2.dnn.DNN_TARGET_CPU
                )
    
                return detector

        except Exception as e:
            self.AppLogFile.writeLogs(str(e))
            return None

        

