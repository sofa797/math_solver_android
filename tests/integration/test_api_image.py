import pytest


@pytest.mark.integration
def test_solve_from_image(client, mocker):
    mocker.patch("backend.main.requests.post").return_value.json.return_value = {
        "md_results": "2x+2=4"
    }
    response = client.post("/solve-from-image", json={
        "image_base64": "fake",
        "user_id": "user6"
    })
    assert response.status_code == 200
    assert response.json()["solution"] == "x = 1.0"
