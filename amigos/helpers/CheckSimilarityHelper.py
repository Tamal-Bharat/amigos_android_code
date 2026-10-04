import os
import cv2
from helpers.DatabaseHelper import DatabaseHelper
from models.RegisterFaceModel import OperationMessageModel, RegisterFaceModel
from insightface.app import FaceAnalysis
from utilities.Constants import Constants
from utilities.LogFile import LogFile

class CheckSimilarityHelper():

    #InsightFace Initialization
    app = FaceAnalysis(name="buffalo_l")
    app.prepare(ctx_id=-1)      # CPU

    databaseHelper = DatabaseHelper()
    AppConstants = Constants()
    AppLogFile = LogFile()

    def checkSimilarity(self)-> RegisterFaceModel | None:

        try:

            image_path = os.path.join(self.AppConstants.IMAGE_FRAME_FOLDER, self.AppConstants.IMAGE_FRAME_NAME)
            image = cv2.imread(image_path)

            if image is None:
                raise Exception("Could not read image") 

            else:                
                faces = self.app.get(image)

                if not faces:
                    raise Exception("No face detected")

                else:              
                    
                    if len(faces) != 1:
                        raise Exception("Multiple faces detected")

                    else:                      
                        # Liveliness check start here

                        # Extract face embedding
                            embedding = faces[0].embedding

                            results = self.databaseHelper.checkFaceSimilarity(embedding)

                            if results is None:
                                self.AppLogFile.writeAttandanceLogs("No face registered in database")

                            else:
                                #int distenceThreshold = 
                                for erp_id, emp_name, distance in results:
                                    similarity = (1 - distance) * 100
                                    print(f"{erp_id} | {emp_name} | Similarity: {similarity:.2f}%")

                                registerFaceModel = RegisterFaceModel(
                                    code=200,
                                    message = OperationMessageModel(
                                        opCode="S",
                                        opMessage="Similarity Check Successful"
                                    )
                                )

                                return registerFaceModel


        except Exception as e:
            self.AppLogFile.writeAttandanceLogs(str(e))
            return None

    
