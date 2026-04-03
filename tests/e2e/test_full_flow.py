import pytest


@pytest.mark.e2e
def test_full_flow(client):
    client.post("/solve", json={
        "equation": "2x=4",
        "user_id": "user78"
    })

    response = client.get("/history", params={
        "user_id": "user78"
    })

    data = response.json()
    assert len(data) == 1
    assert data[0]["equation"] == "2x=4"
