from backend.services.solver import solve_linear
import pytest


@pytest.mark.unit
def test_simple_equation():
    assert solve_linear("2x+2=0") == "x = -1.0"


@pytest.mark.unit
def test_negative():
    assert solve_linear("x-5=0") == "x = 5.0"


@pytest.mark.unit
def test_no_solutions():
    assert solve_linear("x=x+1") == "No solutions"


@pytest.mark.unit
def test_infinite_solutions():
    assert solve_linear("x=x") == "Infinite solutions"


@pytest.mark.unit
def test_invalid():
    assert solve_linear("2x+2") == "Equation must contain ="
