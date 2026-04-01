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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.CivicIssueTheme
import com.simats.civicissue.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {}
) {
    var notifications by remember { mutableStateOf<List<CivicNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            notifications = RetrofitClient.instance.getNotifications()
        } catch (_: Exception) { }
        finally { isLoading = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                RetrofitClient.instance.markAllNotificationsRead()
                                notifications = notifications.map { it.copy(isRead = true) }
                            } catch (_: Exception) {}
                        }
                    }) {
                        Text("Mark all read", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF0F4F8)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsNone, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications available", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(notifications) { notification ->
                        val info = notification.toAdminNotificationInfo()
                        AdminNotificationItem(
                            notification = info,
                            isRead = notification.isRead,
                            onClick = {
                                if (!notification.isRead) {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.markNotificationRead(notification.id)
                                            notifications = notifications.map {
                                                if (it.id == notification.id) it.copy(isRead = true) else it
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNotificationItem(
    notification: AdminNotificationInfo,
    isRead: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread Indicator
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 0.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Left Icon
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = notification.iconBgColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = notification.icon,
                        contentDescription = null,
                        tint = notification.iconBgColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        minLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = if (notification.time.isEmpty()) "Just now" else notification.time,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.wrapContentWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.description,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class AdminNotificationInfo(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

fun CivicNotification.toAdminNotificationInfo(): AdminNotificationInfo {
    val lowerTitle = title.lowercase()
    val lowerType = type.lowercase()
    val (icon, color) = when {
        lowerType.contains("pothole") || lowerTitle.contains("pothole") -> Icons.Default.Warning to Color(0xFFF59E0B)
        lowerType == "status_update" -> Icons.Default.RotateRight to PrimaryBlue
        lowerType.contains("assigned") || lowerTitle.contains("assigned") -> Icons.Default.Engineering to Color(0xFF6366F1)
        lowerType.contains("resolved") || lowerTitle.contains("resolved") -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        lowerType == "complaint_created" || lowerTitle.contains("new") -> Icons.Default.AddCircle to Color(0xFF0EA5E9)
        (priority.lowercase()) == "high" || lowerType == "alert" -> Icons.Default.PriorityHigh to Color(0xFFD32F2F)
        else -> Icons.Default.Notifications to Color(0xFF757575)
    }
    
    return AdminNotificationInfo(
        title = title,
        description = message,
        time = createdAt?.let { formatCivicNotificationDate(it) } ?: "Just now",
        icon = icon,
        iconBgColor = color
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    CivicIssueTheme {
        NotificationScreen()
    }
}
