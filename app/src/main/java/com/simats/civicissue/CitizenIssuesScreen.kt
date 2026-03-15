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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenIssuesScreen(
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Home, 1: Issues, 2: Profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Issues", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on Issues */ },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Issues") },
                    label = { Text("Issues", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Text(
                    "Complaint History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(citizenComplaints) { complaint ->
                ComplaintHistoryCard(complaint)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ComplaintHistoryCard(complaint: CitizenComplaintDetailed) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                complaint.icon,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = complaint.category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Ticket: #${complaint.id}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                StatusBadge(complaint.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User and Time details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(complaint.userName, fontSize = 12.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(complaint.uploadTime, fontSize = 12.sp, color = Color.DarkGray)
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "Status Timeline",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                complaint.timeline.forEachIndexed { index, event ->
                    TimelineEntry(
                        title = event.status,
                        date = event.date,
                        isLast = index == complaint.timeline.size - 1,
                        isCompleted = true
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Show Details",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun TimelineEntry(title: String, date: String, isLast: Boolean, isCompleted: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) PrimaryBlue else Color.LightGray)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (isCompleted) PrimaryBlue.copy(alpha = 0.5f) else Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (!isLast) 16.dp else 0.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) Color.Black else Color.DarkGray
            )
            Text(
                date,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

data class CitizenComplaintDetailed(
    val id: String,
    val category: String,
    val status: String,
    val userName: String,
    val uploadTime: String,
    val icon: ImageVector,
    val timeline: List<TimelineEvent>
)

data class TimelineEvent(
    val status: String,
    val date: String
)

val citizenComplaints = listOf(
    CitizenComplaintDetailed(
        "4281", "Pothole Repair", "Pending", "Citizen Vastr", "Today, 10:30 AM", Icons.Default.ReportProblem,
        listOf(
            TimelineEvent("Complaint Submitted", "March 3, 2026, 10:30 AM")
        )
    ),
    CitizenComplaintDetailed(
        "3952", "Street Light", "In Progress", "Citizen Vastr", "Yesterday, 6:45 PM", Icons.Default.Lightbulb,
        listOf(
            TimelineEvent("Complaint Submitted", "March 2, 2026, 06:45 PM"),
            TimelineEvent("Task Assigned to Officer", "March 2, 2026, 08:30 PM"),
            TimelineEvent("Work In Progress", "March 3, 2026, 09:00 AM")
        )
    ),
    CitizenComplaintDetailed(
        "3104", "Garbage Clearance", "Resolved", "Citizen Vastr", "Feb 28, 09:15 AM", Icons.Default.DeleteOutline,
        listOf(
            TimelineEvent("Complaint Submitted", "Feb 28, 2026, 09:15 AM"),
            TimelineEvent("Under Investigation", "Feb 28, 2026, 11:00 AM"),
            TimelineEvent("Resolved Successfully", "March 1, 2026, 04:30 PM")
        )
    )
)
