import pytest


@pytest.mark.integration
def test_solve_from_image(client, mocker):
    mock_response = mocker.Mock()
    mock_response.json.return_value = {"md_results": "2x+2=4"}
    mock_post = mocker.patch("backend.main.requests.post", return_value=mock_response)
    response = client.post("/solve-from-image", json={
        "image_base64": "fake",
        "user_id": "user6"
    })
    assert response.status_code == 200
    assert response.json()["solution"] == "x = 1.0"

    mock_post.assert_called_once()
    args, kwargs = mock_post.call_args
    assert "Authorization" in kwargs["headers"]
    assert "glm-ocr" in kwargs["json"]["model"]


@pytest.mark.integration
def test_image_no_equation(client, mocker):
    mock_response = mocker.Mock()
    mock_response.json.return_value = {"md_results": "hello world"}
    mocker.patch("backend.main.requests.post", return_value=mock_response)
    response = client.post("/solve-from-image", json={
        "image_base64": "fake",
        "user_id": "user1"
    })
    assert response.status_code == 200
    assert response.json()["equation"] == ""


@pytest.mark.integration
def test_image_api_failure(client, mocker):
    mocker.patch("backend.main.requests.post", side_effect=Exception("api error"))
    response = client.post("/solve-from-image", json={
        "image_base64": "fake",
        "user_id": "user10"
    })
    assert response.status_code == 502
    assert response.json()["detail"] == "ocr service failed"
