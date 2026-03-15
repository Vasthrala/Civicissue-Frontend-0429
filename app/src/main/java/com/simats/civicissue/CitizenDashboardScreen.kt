 package com.simats.civicissue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.CivicIssueTheme
import com.simats.civicissue.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    onReportIssue: () -> Unit = {},
    onViewMyIssues: () -> Unit = {},
    onActiveIssuesClick: () -> Unit = {},
    onResolvedIssuesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAIChatClick: () -> Unit = {}
) {
    var reports by remember { mutableStateOf<List<CitizenReport>>(recentReports.take(3)) }
    var activeIssuesCount by remember { mutableStateOf("03") }
    var resolvedIssuesCount by remember { mutableStateOf("12") }

    // Simplified data loading logic - using mock data for now to fix build errors
    LaunchedEffect(Unit) {
        val active = allComplaints.count { 
            it.status == ComplaintStatus.UNASSIGNED || 
            it.status == ComplaintStatus.ASSIGNED || 
            it.status == ComplaintStatus.IN_PROGRESS
        }
        val resolved = allComplaints.count { 
            it.status == ComplaintStatus.RESOLVED || 
            it.status == ComplaintStatus.COMPLETED
        }
        activeIssuesCount = active.toString().padStart(2, '0')
        resolvedIssuesCount = resolved.toString().padStart(2, '0')
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAIChatClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on Home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 12.sp) },
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
                    onClick = onViewMyIssues,
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "My Complaints") },
                    label = { Text("Reports", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
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
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Top Bar: Welcome, Notifications, Logout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome,",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Citizen Vastr",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Main Action Card: Report New Issue
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReportIssue() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Report New Issue",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Take a photo and report a civic issue",
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Quick Stats Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CitizenStatCard(
                        modifier = Modifier.weight(1f).clickable { onActiveIssuesClick() },
                        label = "Active Issues",
                        value = activeIssuesCount,
                        icon = Icons.Default.ErrorOutline,
                        color = Color(0xFFF59E0B)
                    )
                    CitizenStatCard(
                        modifier = Modifier.weight(1f).clickable { onResolvedIssuesClick() },
                        label = "Resolved",
                        value = resolvedIssuesCount,
                        icon = Icons.Default.Verified,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Recent Complaints Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Recent Complaints",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "View All",
                        fontSize = 14.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onViewMyIssues() }
                    )
                }
            }

            items(reports.take(3)) { report ->
                CitizenReportItem(report)
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
fun CitizenStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CitizenReportItem(report: CitizenReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        report.icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    report.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    report.date,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
            StatusBadge(report.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Resolved" -> Color(0xFF4CAF50)
        "In Progress" -> Color(0xFF2196F3)
        "Pending" -> Color(0xFFFFA000)
        else -> Color.DarkGray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

data class CitizenReport(
    val title: String,
    val date: String,
    val status: String,
    val icon: ImageVector
)

val recentReports = listOf(
    CitizenReport("Pothole reported at Main St.", "Today, 10:30 AM", "Pending", Icons.Default.ReportProblem),
    CitizenReport("Street light repair needed", "Yesterday", "In Progress", Icons.Default.Lightbulb),
    CitizenReport("Garbage clearance requested", "2 days ago", "Resolved", Icons.Default.DeleteOutline)
)

@Preview(showBackground = true)
@Composable
fun CitizenDashboardScreenPreview() {
    CivicIssueTheme {
        CitizenDashboardScreen()
    }
}
