from sqlalchemy import Column, Integer, String, Text, TIMESTAMP
from sqlalchemy.sql import func
from database import Base


class History(Base):
    __tablename__ = "history_queries"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String, index=True)
    equation = Column(Text)
    solution = Column(Text)
    created_at = Column(TIMESTAMP, server_default=func.now())
