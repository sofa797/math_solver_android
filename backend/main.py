from fastapi import FastAPI
from pydantic import BaseModel
import requests
import base64
import re
import os
import psycopg2
from dotenv import load_dotenv

load_dotenv()


app = FastAPI()

HF_API_KEY = os.getenv("HF_API_KEY")

class ImageRequest(BaseModel):
    image_base64: str


class EquationRequest(BaseModel):
    equation: str


def solve_linear(equation: str) -> str:
    equation = equation.replace(" ", "")
    if "=" not in equation:
        return "Equation must contain ="
    left, right = equation.split("=")

    def parse(side):
        a, b = 0, 0
        tokens = re.findall(r'([+-]?[^+-]+)', side)
        for t in tokens:
            if "x" in t:
                coeff = t.replace("x", "")
                if coeff in ["", "+"]:
                    a += 1
                elif coeff == "-":
                    a -= 1
                else:
                    a += float(coeff)
            else:
                b += float(t)
        return a, b

    aL, bL = parse(left)
    aR, bR = parse(right)
    a = aL - aR
    b = bL - bR
    if a == 0:
        return "Infinite solutions" if b == 0 else "No solutions"
    x = -b / a
    return f"x = {x}"


def extract_equation(text: str) -> str:
    matches = re.findall(r'[0-9xX+\-*/^().=]+', text)
    for m in matches:
        if "=" in m and "x" in m:
            return m.strip()
    return ""


def get_db():
    return psycopg2.connect(
        host=os.getenv("DB_HOST"),
        database=os.getenv("DB_NAME"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        port=os.getenv("DB_PORT")
    )


def save_to_db(equation: str, solution: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        """
        INSERT INTO history (equation, solution)
        VALUES (%s, %s)
        """, (equation, solution)
    )
    conn.commit()
    cur.close()
    conn.close()


@app.post("/solve-from-image")
def solve_from_image(data: ImageRequest):
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

    save_to_db(equation, solution)

    return {
        "equation": equation,
        "solution": solution
    }


@app.post("/solve")
def solve(data: EquationRequest):
    equation = data.equation.strip()
    solution = solve_linear(data.equation.strip())
    save_to_db(equation, solution)
    return {
        "equation": data.equation,
        "solution": solution
    }