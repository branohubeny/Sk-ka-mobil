import os
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from typing import Optional

from fastapi import Depends, FastAPI, HTTPException, status
from pydantic import BaseModel, ConfigDict
from sqlalchemy import Column, DateTime, Integer, String, create_engine
from sqlalchemy.orm import Session, declarative_base, sessionmaker

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./legal_ai.db")
engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {},
    pool_pre_ping=True,
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def utc_now():
    return datetime.now(timezone.utc)


class LegalDocument(Base):
    __tablename__ = "legal_documents"

    id = Column(Integer, primary_key=True, index=True)
    document_number = Column(String, index=True, nullable=False)
    title = Column(String, nullable=False)
    document_type = Column(String, nullable=False)
    jurisdiction = Column(String, nullable=False)
    authority = Column(String, nullable=False)
    publication_date = Column(DateTime, nullable=True)
    source_url = Column(String, nullable=True)
    source_hash = Column(String, nullable=True)
    created_at = Column(DateTime, default=utc_now, nullable=False)
    updated_at = Column(DateTime, default=utc_now, onupdate=utc_now, nullable=False)


class LegalDocumentBase(BaseModel):
    document_number: str
    title: str
    document_type: str
    jurisdiction: str = ""
    authority: str = ""
    publication_date: Optional[datetime] = None
    source_url: Optional[str] = None
    source_hash: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)


class LegalDocumentCreate(LegalDocumentBase):
    pass


class LegalDocumentRead(LegalDocumentBase):
    id: int
    created_at: datetime
    updated_at: datetime


Base.metadata.create_all(bind=engine)


@asynccontextmanager
async def lifespan(_app: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(lifespan=lifespan)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


@app.post("/legal-documents", response_model=LegalDocumentRead, status_code=status.HTTP_201_CREATED)
def create_legal_document(document: LegalDocumentCreate, db: Session = Depends(get_db)):
    existing = (
        db.query(LegalDocument)
        .filter(LegalDocument.document_number == document.document_number)
        .first()
    )
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A legal document with this document number already exists.",
        )

    now = utc_now()
    db_document = LegalDocument(**document.model_dump(), created_at=now, updated_at=now)
    db.add(db_document)
    db.commit()
    db.refresh(db_document)
    return db_document


@app.get("/legal-documents", response_model=list[LegalDocumentRead])
def list_legal_documents(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return db.query(LegalDocument).offset(skip).limit(limit).all()


@app.get("/legal-documents/{document_id}", response_model=LegalDocumentRead)
def get_legal_document(document_id: int, db: Session = Depends(get_db)):
    document = db.query(LegalDocument).filter(LegalDocument.id == document_id).first()
    if document is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Legal document not found")
    return document
