import cv2
import os
from aihelpers.AIModelHelper import AIModelHelper
from models.RegisterFaceModel import OperationMessageModel, RegisterFaceModel
from utilities.Constants import Constants
from utilities.LogFile import LogFile

class ExtractFrameHelper():   

    AppConstants = Constants()
    AppLogFile = LogFile()

    def extractFrameFromVideo(self, video_file_path:str)-> str | None:
        
        count = 0
        saved = 0
        no_face_count = 0
        frame_number = 0
        checked = 0
        no_face = 0
        return_text = ""

        aIModelHelper = AIModelHelper()      

        try:
            os.makedirs("frames", exist_ok=True)

            # Delete all files under frames folder
            for file_name in os.listdir(self.AppConstants.FRAME_OUTPUT_DIR):
                file_path = os.path.join(self.AppConstants.FRAME_OUTPUT_DIR, file_name)

                if os.path.isfile(file_path) or os.path.islink(file_path):
                    os.remove(file_path)

            # Open video
            video = cv2.VideoCapture(video_file_path)

            if not video.isOpened():                
                self.AppLogFile.writeLogs("ERROR: Could not open video") 
                raise Exception("ERROR: Could not open video")

            # Get video information
            fps = int(video.get(cv2.CAP_PROP_FPS))
            print("FPS:", fps)

            if fps <= 0:
                video.release()     
                self.AppLogFile.writeLogs("ERROR: Could not determine FPS")        
                raise Exception("ERROR: Could not determine FPS")
                

            # Detector for Face Image Analysis
            detector = aIModelHelper.getFaceDetector()    

            if detector is None:
                self.AppLogFile.writeLogs("Detector is None")  
                raise Exception("Detector is None")

            else:                      
                total_frames = int(video.get(cv2.CAP_PROP_FRAME_COUNT))
                duration = total_frames / fps  
                interval_frames = max(1, int(fps * self.AppConstants.CHECK_INTERVAL_SECONDS))                    

                # Process video
                while True:
                    ret, frame = video.read()
                    
                    if not ret:
                        self.AppLogFile.writeLogs("frame could not be read")  
                        break             

                    # Check only every 3 seconds
                    if frame_number % interval_frames == 0:

                        checked += 1
                        height, width = frame.shape[:2]

                        # YuNet needs the actual frame size
                        detector.setInputSize((width, height))

                        # Detect faces
                        _, faces = detector.detect(frame)                       

                        # Face found
                        if faces is not None:
                            
                            if len(faces) == 1:
                                # Get first/highest-confidence face
                                best_face = max(
                                    faces,
                                    key=lambda face: face[14]
                                )
        
                                confidence = best_face[14]
                                
                                filename = os.path.join(
                                    self.AppConstants.FRAME_OUTPUT_DIR,
                                    f"frame_{saved:04d}.jpg"
                                )
        
                                cv2.imwrite(filename, frame)       
                               
                                self.AppLogFile.writeLogs(
                                    f"[FACE] Time: "
                                    f"{frame_number / fps:.2f}s | "
                                    f"Confidence: {confidence:.2f} | "
                                    f"Saved: {filename}"
                                ) 
        
                                saved += 1

                            else:    
                                self.AppLogFile.writeLogs("Frame has multiple faces")                        

                        else:
                            no_face += 1
                            self.AppLogFile.writeLogs("Frame has no faces")
                            raise Exception("Frame has no faces")                            
    
                    frame_number += 1

                #Release video
                video.release()        

                '''
                print("================================")
                print("PROCESSING COMPLETE")
                print("================================")
                print("Frames checked:", checked)
                print("Frames with face:", saved)
                print("Frames without face:", no_face)
                print("Saved directory:", self.AppConstants.FRAME_OUTPUT_DIR)
                print("================================")
                '''

                
                if saved == 0:                    
                    self.AppLogFile.writeLogs("RESULT: No face detected in the video")
                    return "0"
                else:
                    self.AppLogFile.writeLogs("RESULT: Face detected in the video")
                    return f"{saved}"              
                

        except Exception as e:
            self.AppLogFile.writeLogs(str(e)) 
            return None

        
