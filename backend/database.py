from sqlalchemy import create_engine, Column, String, Float, Integer, ForeignKey, DateTime, Text, Enum
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
import datetime
import enum

SQLALCHEMY_DATABASE_URL = "sqlite:///./civic_issue.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

class PriorityEnum(str, enum.Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"

class StatusEnum(str, enum.Enum):
    UNASSIGNED = "UNASSIGNED"
    ASSIGNED = "ASSIGNED"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    RESOLVED = "RESOLVED"

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String)
    email = Column(String, unique=True, index=True)
    phone_number = Column(String)
    password = Column(String)
    role = Column(String) # 'citizen' or 'admin'

class Complaint(Base):
    __tablename__ = "complaints"
    id = Column(String, primary_key=True, index=True)
    citizen_name = Column(String)
    title = Column(String)
    category = Column(String)
    location = Column(String)
    date_time = Column(String) # String to match frontend format
    priority = Column(String)
    status = Column(String)
    description = Column(Text)
    image_urls = Column(String) # Comma separated
    officer_name = Column(String, nullable=True)
    severity_level = Column(String, default="Medium")
    urgency_level = Column(String, default="Normal")
    latitude = Column(Float)
    longitude = Column(Float)

def init_db():
    Base.metadata.create_all(bind=engine)
