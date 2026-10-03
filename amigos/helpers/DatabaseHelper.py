import cv2
import psycopg2
import os
import numpy as np
from utilities.Constants import Constants
from utilities.LogFile import LogFile

class DatabaseHelper():

    AppConstants = Constants()   
    AppLogFile = LogFile()  
        
    def getDbConnObj(self):

        try:
            dbConnectObj = psycopg2.connect(
                host = self.AppConstants.DATABASE_HOST,
                port = self.AppConstants.DATABASE_PORT,
                database = self.AppConstants.DATABASE_NAME,
                user = self.AppConstants.DATABASE_USER,
                password = self.AppConstants.DATABASE_PASSWORD          
            )

            return dbConnectObj

        except Exception as e:
            self.AppLogFile.writeLogs(str(e))
            return None

    def saveFaceRegRecord(self, embedding, emp_id, emp_name)-> int | None: 

        try:          
            #embeddings = []       

            conn = self.getDbConnObj()

            if conn is None:
                self.AppLogFile.writeLogs("DB Connection is None")
                raise Exception("DB Connection is None") 

            else:
                cursor = conn.cursor()

                cursor.execute(
                    """
                    INSERT INTO image_embeddings(erp_id, emp_name, embedding)
                    VALUES (%s, %s, %s)
                    """,
                    (
                        emp_id,
                        emp_name,
                        embedding.astype(np.float32).tolist(),
                    ),
                )

                self.AppLogFile.writeLogs("Embeddings stored successfully")
                conn.commit()
                cursor.close()
                conn.close()

                return 1

        except Exception as e:
            self.AppLogFile.writeLogs(str(e))
            #conn.commit()
            #cursor.close()
            #conn.close()
            return None
            
        


    def checkFaceSimilarity(self, embedding)-> list[tuple]:

        conn = self.getDbConnObj()
        cursor = conn.cursor()     

        # ----------------------------
        # Similarity Search
        # ----------------------------
        cursor.execute(
            """
            SELECT
                erp_id,
                emp_name,
                embedding <=> %s::vector AS distance
            FROM image_embeddings
            ORDER BY embedding <=> %s::vector
            LIMIT 5;
            """,
            (
                embedding.tolist(),
                embedding.tolist(),
            ),
        )

        results = cursor.fetchall()  

        conn.commit()
        cursor.close()
        conn.close()     

        return results