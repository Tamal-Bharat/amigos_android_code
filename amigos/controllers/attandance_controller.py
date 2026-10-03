from fastapi import FastAPI, Request, UploadFile, File, Form
import os
from models.RegisterFaceModel import OperationMessageModel, RegisterFaceModel
from helpers.VideoReadWriteHelper import VideoReadWriteHelper
from helpers.ImageFileReadWriteHelper import ImageFileReadWriteHelper
from models.RegisterFaceModel import RegisterFaceModel
from utilities.Constants import Constants
from utilities.LogFile import LogFile


app = FastAPI()
AppConstants = Constants()
#Controller for register faces from video clips
@app.post("/attandance/register-face", response_model=RegisterFaceModel)
async def registerFace(video:UploadFile = File(...), emp_id:str = Form(...), emp_name: str = Form(...)): 

    try:  
        AppLogFile = LogFile()

        AppLogFile.writeLogs("\n")
        AppLogFile.writeLogs("****************************")
        AppLogFile.writeLogs("Log File Created")
        AppLogFile.writeLogs(f"Request from: {emp_id} - {emp_name}")

        videoReadWriteHelper = VideoReadWriteHelper()       

        registerFaceModel =  await videoReadWriteHelper.saveVideo(video, emp_id, emp_name)        

        if registerFaceModel is None:
            raise Exception("Some Exception occured")
        else:
            return registerFaceModel        

    except Exception as e:
        AppLogFile.writeLogs(f"Exception in: {registerFace}")
        AppLogFile.writeLogs(str(e))

        registerFaceModel = RegisterFaceModel(
            code = 404,
            message = OperationMessageModel(
                opCode="F",
                opMessage=str(e)
            )
        )

        return registerFaceModel

#Controller for face similarity check from frame
@app.post("/attandance/check-similarity")
async def checkSimilarity(image: UploadFile = File(...)):

    imageFileReadWriteHelper = ImageFileReadWriteHelper()

    if not image.content_type or not image.content_type.startswith("image/"):
        registerFaceModel = RegisterFaceModel(
            code = 400,
            message = "Invalid image format"
        )

    else:
        registerFaceModel = await imageFileReadWriteHelper.saveFrameImage(image)   

    return registerFaceModel
    
