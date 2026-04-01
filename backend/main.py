from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
import requests
import os


from database import SessionLocal, engine, Base
import schemas, crud
from services.solver import solve_linear, extract_equation

Base.metadata.create_all(bind=engine)

app = FastAPI()

HF_API_KEY = os.getenv("HF_API_KEY")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.post("/solve")
def solve(data: schemas.EquationRequest, db: Session = Depends(get_db)):
    equation = data.equation.strip()
    solution = solve_linear(equation)
    crud.save_history(db, equation, solution, data.user_id)
    return {
        "equation": equation,
        "solution": solution
    }


@app.post("/solve-from-image")
def solve_from_image(data: schemas.ImageRequest, db: Session = Depends(get_db)):
    response = requests.post(
        "https://router.huggingface.co/zai-org/api/paas/v4/layout_parsing",
        headers={"Authorization": f"Bearer {HF_API_KEY}"},
        json={
            "model": "glm-ocr",
            "file": f"data:image/png;base64,{data.image_base64}"
        }
    )
    result = response.json()
    text = result.get("md_results", "")
    equation = extract_equation(text)
    solution = solve_linear(equation)
    crud.save_history(db, equation, solution, data.user_id)
    return {
        "equation": equation,
        "solution": solution
    }


@app.get("/history")
def get_history(user_id: str, db: Session = Depends(get_db)):
    records = crud.get_history(db, user_id)
    return records
