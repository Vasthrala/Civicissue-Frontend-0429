package com.simats.civicissue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.PrimaryBlue
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    onBack: () -> Unit,
    onAssignOfficer: () -> Unit,
    onUpdateStatus: (ComplaintStatus) -> Unit,
    onResolveClick: (String) -> Unit = {}
) {
    // Mock data for display
    val complaint = remember(complaintId) {
        allComplaints.find { it.id == complaintId } ?: allComplaints[0]
    }

    // Calculate Similar Issues dynamically (within 2km radius)
    val similarIssues = remember(complaint) {
        allComplaints
            .filter { it.id != complaint.id }
            .map { other ->
                val dist = calculateDistance(complaint.latitude, complaint.longitude, other.latitude, other.longitude)
                Pair(other.id, dist)
            }
            .filter { pair -> pair.second <= 2.0 }
            .sortedBy { pair -> pair.second }
            .take(3)
            .map { pair ->
                val id = pair.first
                val dist = pair.second
                val distStr = if (dist < 1.0) "${(dist * 1000).toInt()}m" else "%.1fkm".format(dist)
                val categoryMatch = allComplaints.find { it.id == id }?.category ?: "Issue"
                Pair(id, "$categoryMatch nearby - $distStr")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Title & Status Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = complaint.id,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        StatusBadge(status = complaint.status)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = complaint.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Quick Info Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(icon = Icons.Default.Category, text = complaint.category)
                    InfoChip(icon = Icons.Default.Person, text = complaint.citizenName)
                    PriorityBadge(priority = complaint.priority)
                }
            }

            // Description Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = complaint.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // AI AI Insights (Severity & Urgency)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF7B1FA2))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("SEVERITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(complaint.severityLevel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("URGENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(complaint.urgencyLevel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                            }
                        }
                    }
                }
            }

            // Image Gallery placeholder
            item {
                Column {
                    Text("Evidence & Nearby Context", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Original Citizen Image
                        item {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray)
                                    .border(2.dp, PrimaryBlue, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(40.dp))
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(PrimaryBlue)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Citizen Upload", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        // Grouped Similar Images (Clustered)
                        items(3) { index ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .padding(4.dp)
                                ) {
                                    Text("AI Match", color = Color.White, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Clustered Issues List
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Similar Nearby Issues (Clustered)", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp, 
                            color = PrimaryBlue
                        )
                        Text(
                            "Issues grouped by visual similarity and proximity", 
                            fontSize = 11.sp, 
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        similarIssues.forEach { pair ->
                            val id = pair.first
                            val desc = pair.second
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(id, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(desc, fontSize = 12.sp, color = Color.DarkGray)
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // Location Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp) )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Location Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = complaint.location, fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Map Preview Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(40.dp))
                                Text("Map View Integration", fontSize = 12.sp, color = PrimaryBlue)
                            }
                        }
                    }
                }
            }

            // Timeline Section
            item {
                Column {
                    Text("Complaint Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimelineItem("Complaint Raised", "Mar 03, 2026 - 09:30 AM", isCompleted = true, isLast = false)
                    TimelineItem("Officer Assigned", "Awaiting Assignment", isCompleted = false, isLast = false)
                    TimelineItem("In Progress", "--", isCompleted = false, isLast = false)
                    TimelineItem("Resolved", "--", isCompleted = false, isLast = true)
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(ComplaintStatus.IN_PROGRESS) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Text("Start Work", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onResolveClick(complaint.id) }, 
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Complete & Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        color = Color(0xFFE8EAF6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryBlue)
        }
    }
}

@Composable
fun TimelineItem(title: String, time: String, isCompleted: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) PrimaryBlue else Color.LightGray)
                    .border(2.dp, Color.White, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isCompleted) PrimaryBlue else Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isCompleted) Color.Black else Color.Gray)
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Helper functions for distance calculation
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2.0).pow(2.0) + 
            cos(Math.toRadians(lat1)) * 
            cos(Math.toRadians(lat2)) * 
            sin(dLon / 2.0).pow(2.0)
    val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    return r * c
}
