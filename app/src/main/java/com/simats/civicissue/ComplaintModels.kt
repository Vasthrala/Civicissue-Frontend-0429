package com.simats.civicissue

import androidx.compose.ui.graphics.Color

data class Complaint(
    val id: String,
    val citizenName: String,
    val title: String,
    val category: String,
    val location: String,
    val dateTime: String,
    val priority: Priority,
    val status: ComplaintStatus,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val officerName: String? = null,
    val severityLevel: String = "Medium",
    val urgencyLevel: String = "Normal",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class Priority(val label: String, val color: Color) {
    HIGH("High", Color(0xFFD32F2F)),
    MEDIUM("Medium", Color(0xFFF9A825)),
    LOW("Low", Color(0xFF388E3C))
}

enum class ComplaintStatus(val label: String) {
    UNASSIGNED("Unassigned"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    RESOLVED("Resolved")
}

data class Officer(
    val id: String,
    val name: String,
    val department: String,
    val workloadCount: Int
)

data class CivicNotification(
    val id: String,
    val complaintId: String,
    val title: String,
    val location: String,
    val time: String,
    val priority: Priority
)

data class CitizenReportDto(
    val title: String,
    val date: String,
    val status: String,
    val icon: String
)

val allComplaints = listOf(
    Complaint(
        id = "#CE-102",
        citizenName = "Rahul Sharma",
        title = "Pothole on Main St.",
        category = "Road",
        location = "Main St, Sector 4",
        dateTime = "Mar 03, 2026 • 09:30 AM",
        priority = Priority.HIGH,
        status = ComplaintStatus.UNASSIGNED,
        description = "A large pothole has formed in the middle of the road. It's causing traffic delays and is dangerous for two-wheelers.",
        severityLevel = "High",
        urgencyLevel = "Immediate",
        latitude = 17.4448,
        longitude = 78.3498
    ),
    Complaint(
        id = "#CE-098",
        citizenName = "Priya Patel",
        title = "Broken Street Light",
        category = "Electricity",
        location = "Oak Avenue, Crossroad",
        dateTime = "Mar 02, 2026 • 07:45 PM",
        priority = Priority.LOW,
        status = ComplaintStatus.ASSIGNED,
        description = "The street light near the entrance of Block B has stopped working, making it very dark at night.",
        severityLevel = "Low",
        urgencyLevel = "Normal",
        latitude = 17.4562,
        longitude = 78.3610,
        officerName = "Amit Kumar"
    ),
    Complaint(
        id = "#CE-095",
        citizenName = "Suresh Kumar",
        title = "Waste Collection Delay",
        category = "Sanitation",
        location = "Green Park Colony",
        dateTime = "Mar 01, 2026 • 11:15 AM",
        priority = Priority.MEDIUM,
        status = ComplaintStatus.UNASSIGNED,
        description = "The garbage truck hasn't visited Willow Road for three days now. Waste is accumulating.",
        severityLevel = "Medium",
        urgencyLevel = "Urgent",
        latitude = 17.4455,
        longitude = 78.3505
    )
)
