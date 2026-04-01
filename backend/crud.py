from sqlalchemy.orm import Session
import models


def save_history(db: Session, equation: str, solution: str, user_id: str):
    record = models.History(
        user_id=user_id,
        equation=equation,
        solution = solution
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


def get_history(db: Session, user_id: str):
    return (
        db.query(models.History)
        .filter(models.History.user_id == user_id)
        .order_by(models.History.created_at.desc())
        .all()
    )
