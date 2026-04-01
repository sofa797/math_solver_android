from pydantic import BaseModel
from datetime import datetime


class ImageRequest(BaseModel):
    image_base64: str
    user_id: str


class EquationRequest(BaseModel):
    equation: str
    user_id: str


class HistoryResponse(BaseModel):
    equation: str
    solution: str
    created_at: datetime

    class Config:
        orm_mode = True
