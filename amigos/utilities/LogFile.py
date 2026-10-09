from datetime import datetime
import os
from utilities.Constants import Constants

class LogFile():

    AppConstants = Constants()

    def writeLogs(self, message:str):

        today = datetime.now().strftime("%d-%m-%Y")
        os.makedirs(self.AppConstants.LOG_FIEL_FOLDER, exist_ok=True)

        file_path = os.path.join(self.AppConstants.LOG_FIEL_FOLDER, f"{today}.txt")

        current_time = datetime.now().strftime("%H:%M:%S")

        with open(file_path, "a", encoding="utf-8") as file:
           file.write(f"[{current_time}] {message}\n")

    
    #Creating and updating logs for similarity check
    def writeAttandanceLogs(self, message:str):

        today = datetime.now().strftime("%d-%m-%Y")
        os.makedirs(self.AppConstants.LOG_FIEL_FOLDER, exist_ok=True)

        file_path = os.path.join(self.AppConstants.LOG_FIEL_FOLDER, f"{today}_attendance.txt")

        current_time = datetime.now().strftime("%H:%M:%S")

        with open(file_path, "a", encoding="utf-8") as file:
            file.write(f"[{current_time}] {message}\n")
