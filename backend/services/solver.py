import re


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
    a = aL-aR
    b = bL-bR
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
