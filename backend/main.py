from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
import database
import uvicorn
from pydantic import BaseModel
import uuid
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic models for request/response
class ComplaintBase(BaseModel):
    citizenName: str
    title: str
    category: str
    location: str
    dateTime: str
    priority: str
    status: str
    description: str = ""
    imageUrls: List[str] = []
    officerName: Optional[str] = None
    severityLevel: str = "Medium"
    urgencyLevel: str = "Normal"
    latitude: float = 0.0
    longitude: float = 0.0

class ComplaintResponse(ComplaintBase):
    id: str

    class Config:
        from_attributes = True

class CitizenReportDto(BaseModel):
    title: str
    date: str
    status: str
    icon: str

class UserCreate(BaseModel):
    fullName: str
    email: str
    phoneNumber: str
    password: str
    role: str = "citizen"

class UserLogin(BaseModel):
    email: str
    password: str
    role: str

# Dependency to get DB session
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.on_event("startup")
def startup():
    database.init_db()
    # Seed data if empty
    db = database.SessionLocal()
    if db.query(database.Complaint).count() == 0:
        seed_data = [
            database.Complaint(
                id="#CE-102",
                citizen_name="Rahul Sharma",
                title="Pothole on Main St.",
                category="Road",
                location="Main St, Sector 4",
                date_time="Mar 03, 2026 • 09:30 AM",
                priority="HIGH",
                status="UNASSIGNED",
                description="A large pothole has formed in the middle of the road.",
                severity_level="High",
                urgency_level="Immediate",
                latitude=17.4448,
                longitude=78.3498
            ),
            database.Complaint(
                id="#CE-098",
                citizen_name="Priya Patel",
                title="Broken Street Light",
                category="Electricity",
                location="Oak Avenue, Crossroad",
                date_time="Mar 02, 2026 • 07:45 PM",
                priority="LOW",
                status="ASSIGNED",
                description="The street light near the entrance of Block B has stopped working.",
                severity_level="Low",
                urgency_level="Normal",
                latitude=17.4562,
                longitude=78.3610,
                officer_name="Amit Kumar"
            )
        ]
        db.add_all(seed_data)
        db.commit()
    db.close()

@app.get("/api/complaints", response_model=List[ComplaintResponse])
def get_complaints(db: Session = Depends(get_db)):
    complaints = db.query(database.Complaint).all()
    # Map from DB model to Pydantic
    results = []
    for c in complaints:
        results.append(ComplaintResponse(
            id=c.id,
            citizenName=c.citizen_name,
            title=c.title,
            category=c.category,
            location=c.location,
            dateTime=c.date_time,
            priority=c.priority,
            status=c.status,
            description=c.description or "",
            imageUrls=c.image_urls.split(",") if c.image_urls else [],
            officerName=c.officer_name,
            severityLevel=c.severity_level,
            urgencyLevel=c.urgency_level,
            latitude=c.latitude,
            longitude=c.longitude
        ))
    return results

@app.post("/api/complaints", response_model=ComplaintResponse)
def create_complaint(complaint: ComplaintBase, db: Session = Depends(get_db)):
    db_complaint = database.Complaint(
        id=f"#CE-{uuid.uuid4().hex[:4].upper()}",
        citizen_name=complaint.citizenName,
        title=complaint.title,
        category=complaint.category,
        location=complaint.location,
        date_time=complaint.dateTime,
        priority=complaint.priority,
        status=complaint.status,
        description=complaint.description,
        image_urls=",".join(complaint.imageUrls),
        officer_name=complaint.officerName,
        severity_level=complaint.severityLevel,
        urgency_level=complaint.urgencyLevel,
        latitude=complaint.latitude,
        longitude=complaint.longitude
    )
    db.add(db_complaint)
    db.commit()
    db.refresh(db_complaint)
    return ComplaintResponse(
        id=db_complaint.id,
        citizenName=db_complaint.citizen_name,
        title=db_complaint.title,
        category=db_complaint.category,
        location=db_complaint.location,
        dateTime=db_complaint.date_time,
        priority=db_complaint.priority,
        status=db_complaint.status,
        description=db_complaint.description or "",
        imageUrls=db_complaint.image_urls.split(",") if db_complaint.image_urls else [],
        officerName=db_complaint.officer_name,
        severityLevel=db_complaint.severity_level,
        urgencyLevel=db_complaint.urgency_level,
        latitude=db_complaint.latitude,
        longitude=db_complaint.longitude
    )

@app.get("/api/reports/recent", response_model=List[CitizenReportDto])
def get_recent_reports(db: Session = Depends(get_db)):
    # Mock data for recent reports, or could derive from complaints
    return [
        CitizenReportDto(title="Pothole reported at Main St.", date="Today, 10:30 AM", status="Pending", icon="ReportProblem"),
        CitizenReportDto(title="Street light repair needed", date="Yesterday", status="In Progress", icon="Lightbulb")
    ]

@app.post("/api/auth/signup")
def signup(user: UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(database.User).filter(database.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    new_user = database.User(
        full_name=user.fullName,
        email=user.email,
        phone_number=user.phoneNumber,
        password=user.password, # In real app, hash this!
        role=user.role
    )
    db.add(new_user)
    db.commit()
    return {"message": "User created successfully"}

@app.post("/api/auth/login")
def login(user: UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(database.User).filter(
        database.User.email == user.email,
        database.User.password == user.password,
        database.User.role == user.role
    ).first()
    
    if not db_user:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    return {"message": "Login successful", "user": {"fullName": db_user.full_name, "email": db_user.email, "role": db_user.role}}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
