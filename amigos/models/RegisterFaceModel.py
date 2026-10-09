from pydantic import BaseModel


class OperationMessageModel(BaseModel):
    opCode: str
    opMessage: str

class RegisterFaceModel(BaseModel):
    code: int
    message: OperationMessageModel