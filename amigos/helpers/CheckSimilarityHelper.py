import os
import cv2
from helpers.DatabaseHelper import DatabaseHelper
from models.RegisterFaceModel import RegisterFaceModel
from insightface.app import FaceAnalysis
from utilities.Constants import Constants

class CheckSimilarityHelper():

    #InsightFace Initialization
    app = FaceAnalysis(name="buffalo_l")
    app.prepare(ctx_id=-1)      # CPU

    databaseHelper = DatabaseHelper()
    AppConstants = Constants()

    def checkSimilarity(self)-> RegisterFaceModel:

        image_path = os.path.join(self.AppConstants.IMAGE_FRAME_FOLDER, self.AppConstants.IMAGE_FRAME_NAME)
        image = cv2.imread(image_path)

        if image is None:
            registerFaceModel = RegisterFaceModel(
                code = 400,
                message = "Could not read image."
            )

            #return registerFaceModel

        faces = self.app.get(image)

        if not faces:
            registerFaceModel = RegisterFaceModel(
                code = 400,
                message = "No face detected."
            )
                        
            #return registerFaceModel
        
            
        if len(faces) != 1:
            registerFaceModel = RegisterFaceModel(
                code = 400,
                message = "Multiple faces detected."
            )
            
            #return registerFaceModel
        
        # Liveliness check start here

        # Extract face embedding
            embedding = faces[0].embedding

            results = self.databaseHelper.checkFaceSimilarity(embedding)

            for erp_id, emp_name, distance in results:
                similarity = (1 - distance) * 100
                print(f"{erp_id} | {emp_name} | Similarity: {similarity:.2f}%")


    

    
