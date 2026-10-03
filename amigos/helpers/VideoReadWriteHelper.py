from fastapi import UploadFile
import os
import cv2
from insightface.app import FaceAnalysis
from models.RegisterFaceModel import OperationMessageModel, RegisterFaceModel
from helpers.ExtractFrameHelper import ExtractFrameHelper
from helpers.DatabaseHelper import DatabaseHelper
from utilities.Constants import Constants
from utilities.LogFile import LogFile


class VideoReadWriteHelper():

    databaseHelper = DatabaseHelper()
    AppConstants = Constants()
    extractFrameHelper = ExtractFrameHelper()
    AppLogFile = LogFile()

    #video_folder = "videos"
    return_message:str = ""

    # Initialize InsightFace
    app = FaceAnalysis(name="buffalo_l")
    app.prepare(ctx_id=-1)      # -1 = CPU, 0 = GP   
            

    async def saveVideo(self, video: UploadFile, emp_id:str, emp_name:str)-> RegisterFaceModel | None: 

        savedEmbeddings: int = 0;      

        try:
        
            data = await video.read()
    
            os.makedirs(self.AppConstants.UPLOAD_VIDEO_FOLDER, exist_ok=True)
            file_path = os.path.join(self.AppConstants.UPLOAD_VIDEO_FOLDER, video.filename)
    
            if os.path.exists(self.AppConstants.UPLOAD_VIDEO_FOLDER):
                for filename in os.listdir(self.AppConstants.UPLOAD_VIDEO_FOLDER):
                    file = os.path.join(self.AppConstants.UPLOAD_VIDEO_FOLDER, filename)
    
                    if os.path.isfile(file):
                        os.remove(file)
    
    
            with open(file_path,"wb") as f:
                f.write(data)

            
            #Extracting the video into multiple frames
            returnValue: str | None = self.extractFrameHelper.extractFrameFromVideo(file_path)

            if returnValue is None:
                 raise Exception("Some Exception occured in extractFrameFromVideo")
                
            else:     

                if returnValue == "0":
                    registerFaceModel = RegisterFaceModel(
                        code = 200,
                        message = OperationMessageModel(
                            opCode = "F",
                            opMessage = "No face detected in the video"
                        )
                    )

                    return registerFaceModel

                else:                         
                    for file in os.listdir(self.AppConstants.FRAME_OUTPUT_DIR):
                    
                        if not file.lower().endswith((".jpg", ".jpeg", ".png")):
                            continue
            
                        image_path = os.path.join(self.AppConstants.FRAME_OUTPUT_DIR, file)
                        img = cv2.imread(image_path)
                        faces = self.app.get(img)

                        # Skip if no face detected
                        if len(faces) == 0:
                            continue

                        # Skip if multiple faces
                        if len(faces) > 1:
                            continue

                        face = faces[0]
                        embedding = face.embedding

                        embaddingSave = self.databaseHelper.saveFaceRegRecord(embedding, emp_id, emp_name)    

                        if embaddingSave is None:
                           self.AppLogFile.writeLogs("No embedding saved")  

                        else:
                            savedEmbeddings +=1                                


                    registerFaceModel = RegisterFaceModel(
                        code = 200,
                        message = OperationMessageModel(
                            opCode = "F",
                            opMessage = f"Embeddings saved: {savedEmbeddings}"
                        )
                    )

                    return registerFaceModel
                
        except Exception as e:
             self.AppLogFile.writeLogs(str(e))
             return None
        
        
