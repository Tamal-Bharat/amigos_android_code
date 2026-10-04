from fastapi import UploadFile, File
import cv2
import numpy as np
import os
from helpers.CheckSimilarityHelper import CheckSimilarityHelper
from models.RegisterFaceModel import RegisterFaceModel
from utilities.Constants import Constants
from utilities.LogFile import LogFile

class ImageFileReadWriteHelper():

    checkSimilarityHelper = CheckSimilarityHelper()
    AppConstants = Constants()
    AppLogFile = LogFile()  

    async def saveFrameImage(self, image: UploadFile = File(...))-> RegisterFaceModel | None:

        try:
            # Read the uploaded image
            image_bytes = await image.read()
    
            if not image_bytes:
                raise Exception("Empty image")

            else:  
                # Decode and validate the image
                frame = cv2.imdecode(
                    np.frombuffer(image_bytes, np.uint8),
                        cv2.IMREAD_COLOR
                )
        
                if frame is None:
                    raise Exception("Unable to decode image")

                else:               
                    os.makedirs(self.AppConstants.IMAGE_FRAME_FOLDER, exist_ok=True)
                    file_path = os.path.join(self.AppConstants.IMAGE_FRAME_FOLDER, self.AppConstants.IMAGE_FRAME_NAME)
            
                    if os.path.exists(self.AppConstants.IMAGE_FRAME_FOLDER):
                        for filename in os.listdir(self.AppConstants.IMAGE_FRAME_FOLDER):
                            file = os.path.join(self.AppConstants.IMAGE_FRAME_FOLDER, filename)
            
                            if os.path.isfile(file):
                                os.remove(file)
            
                    # Save the frame
                    success = cv2.imwrite(file_path, frame)
            
                    if not success:
                        raise Exception("Failed to save frame")                       
            
                    else:                        
                        registerFaceModel = self.checkSimilarityHelper.checkSimilarity()

                        if registerFaceModel is None:
                            return None

                        else:
                            return registerFaceModel   

        
        except Exception as e:
            self.AppLogFile.writeAttandanceLogs(str(e))
            return None





