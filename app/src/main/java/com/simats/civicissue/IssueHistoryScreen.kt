package com.simats.civicissue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueHistoryScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Issue History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Past Civic Reports",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(historyComplaints) { complaint ->
                HistoryComplaintCard(complaint = complaint)
            }
        }
    }
}

@Composable
fun HistoryComplaintCard(complaint: Complaint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        complaint.id,
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(complaint.status)
                }
                PriorityBadge(complaint.priority)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                complaint.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Raised by: ${complaint.citizenName}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = complaint.location,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = complaint.dateTime,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
                
                if (complaint.officerName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Engineering,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resolved by: ${complaint.officerName}",
                            fontSize = 11.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Mock Data for History
val historyComplaints = listOf(
    Complaint(
        id = "#CE-085",
        citizenName = "Arjun Reddy",
        title = "Water Leakage at MG Road",
        category = "Water",
        location = "MG Road, Near Metro Station",
        dateTime = "Feb 20, 2026 • 10:15 AM",
        priority = Priority.HIGH,
        status = ComplaintStatus.RESOLVED,
        officerName = "Amit Kumar"
    ),
    Complaint(
        id = "#CE-072",
        citizenName = "Lakshmi Devi",
        title = "Street Light Repair",
        category = "Electricity",
        location = "Anna Nagar, 4th Street",
        dateTime = "Jan 15, 2026 • 08:45 PM",
        priority = Priority.LOW,
        status = ComplaintStatus.COMPLETED,
        officerName = "Sneha Rao"
    ),
    Complaint(
        id = "#CE-064",
        citizenName = "Karthik Raja",
        title = "Garbage Collection Delay",
        category = "Sanitation",
        location = "Velachery, Main Road",
        dateTime = "Dec 10, 2025 • 09:00 AM",
        priority = Priority.MEDIUM,
        status = ComplaintStatus.RESOLVED,
        officerName = "Zoya Khan"
    ),
    Complaint(
        id = "#CE-051",
        citizenName = "Meera Nair",
        title = "Sewage Overflow",
        category = "Drainage",
        location = "Adyar Circle",
        dateTime = "Nov 05, 2025 • 11:30 AM",
        priority = Priority.HIGH,
        status = ComplaintStatus.COMPLETED,
        officerName = "Vikram Singh"
    )
)
