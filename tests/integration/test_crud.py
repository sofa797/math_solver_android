from backend import crud
import pytest


@pytest.mark.integration
def test_save_and_get_history(db):
    crud.save_history(db, "2x=4", "x=2", "user1")
    records = crud.get_history(db, "user1")
    assert len(records) == 1
    assert records[0].equation == "2x=4"
    assert records[0].solution == "x=2"
