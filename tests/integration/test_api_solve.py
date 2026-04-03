import pytest


@pytest.mark.integration
def test_solve_endpoint(client):
    response = client.post("/solve", json ={
        "equation": "4x=16",
        "user_id": "user8"
    })
    assert response.status_code == 200
    data = response.json()
    assert data["solution"] == "x = 4.0"
