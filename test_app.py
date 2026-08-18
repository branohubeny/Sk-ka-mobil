import uuid
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

from app import Base, app, engine


@pytest.fixture(autouse=True)
def reset_database():
    db_path = Path("legal_ai.db")
    if db_path.exists():
        db_path.unlink()
    engine.dispose()
    Base.metadata.create_all(bind=engine)
    yield
    if db_path.exists():
        db_path.unlink()
    engine.dispose()


client = TestClient(app)


def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}


def test_create_and_list_documents():
    document_number = f"DOC-{uuid.uuid4().hex[:8]}"
    payload = {
        "document_number": document_number,
        "title": "Test legal document",
        "document_type": "law",
        "jurisdiction": "Slovakia",
        "authority": "Ministry of Justice",
        "source_url": "https://example.com/doc/1",
    }

    create_response = client.post("/legal-documents", json=payload)
    assert create_response.status_code == 201
    data = create_response.json()
    assert data["document_number"] == payload["document_number"]
    assert data["title"] == payload["title"]

    list_response = client.get("/legal-documents")
    assert list_response.status_code == 200
    items = list_response.json()
    assert any(item["document_number"] == payload["document_number"] for item in items)
