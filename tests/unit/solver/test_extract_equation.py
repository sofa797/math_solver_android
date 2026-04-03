from backend.services.solver import extract_equation
import pytest


@pytest.mark.unit
def test_extract_basic():
    text = "Solve this: 2x+3=5"
    assert extract_equation(text) == "2x+3=5"


@pytest.mark.unit
def test_extract_multiple():
    text = "just 3+3=6 and 5x=10"
    assert extract_equation(text) == "5x=10"


@pytest.mark.unit
def test_no_equation():
    assert extract_equation("hello") == ""
