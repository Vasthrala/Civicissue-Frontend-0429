package com.simats.civicissue

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.civicissue.ui.theme.CivicIssueTheme
import com.simats.civicissue.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

// Color Definitions
val BackgroundGrey = Color(0xFFF0F4F8)
val SuccessGreen = Color(0xFF4CAF50)
val OrderIndigo = Color(0xFF6366F1)
val WarningOrange = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerNotificationScreen(
    onBack: () -> Unit = {}
) {
    var notifications by remember { mutableStateOf<List<CivicNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedNotification by remember { mutableStateOf<CivicNotification?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            notifications = RetrofitClient.instance.getNotifications()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load notifications"
            Log.e("OfficerNotifications", "Load failed", e)
        } finally {
            isLoading = false
        }
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
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    RetrofitClient.instance.markAllNotificationsRead()
                                    notifications = notifications.map { it.copy(isRead = true) }
                                } catch (_: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Text(
                            "Mark all read",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = BackgroundGrey
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
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
                        val info = notification.toOfficerNotificationInfo()
                        OfficerNotificationItem(notification = info, isRead = notification.isRead) {
                            selectedNotification = notification
                            showSheet = true
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
                    }
                }
            }
        }

        if (showSheet && selectedNotification != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                selectedNotification?.let { OfficerNotificationDetailContent(it.toOfficerNotificationInfo()) }
            }
        }
    }
}

@Composable
fun OfficerNotificationItem(
    notification: OfficerNotificationInfo,
    isRead: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debouncedClickable { onClick() },
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
                // To keep alignment same for read/unread
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Left: Icon with soft background
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = notification.color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = notification.icon,
                        contentDescription = null,
                        tint = notification.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center content
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
                    text = notification.message,
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

@Composable
fun OfficerNotificationDetailContent(notification: OfficerNotificationInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = notification.color.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.color,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = notification.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Text(
            text = notification.time,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = notification.message,
            fontSize = 16.sp,
            color = Color(0xFF374151),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        if (notification.image != null || !notification.imageUrl.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Attachment Proof:",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            if (notification.image != null) {
                Image(
                    bitmap = notification.image.asImageBitmap(),
                    contentDescription = "Attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
                val fullUrl = if (notification.imageUrl!!.startsWith("http")) notification.imageUrl else "$baseUrl${notification.imageUrl}"
                AsyncImage(
                    model = fullUrl,
                    contentDescription = "Attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* Detail Action */ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Acknowledge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

data class OfficerNotificationInfo(
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    val image: android.graphics.Bitmap? = null,
    val imageUrl: String? = null
)

fun CivicNotification.toOfficerNotificationInfo(): OfficerNotificationInfo {
    val lowerTitle = title.lowercase()
    val lowerType = type.lowercase()
    
    val (icon, color) = when {
        lowerType == "status_update" -> Icons.Default.RotateRight to PrimaryBlue
        lowerType.contains("assigned") || lowerTitle.contains("assigned") -> Icons.Default.PersonSearch to OrderIndigo
        lowerType.contains("resolved") || lowerTitle.contains("resolved") || lowerTitle.contains("completed") -> Icons.Default.CheckCircle to SuccessGreen
        lowerType == "complaint_created" || lowerTitle.contains("new") -> Icons.Default.PostAdd to Color(0xFF0EA5E9)
        lowerType == "alert" || lowerTitle.contains("urgent") -> Icons.Default.Error to Color(0xFFD32F2F)
        else -> Icons.Default.Notifications to WarningOrange
    }
    
    return OfficerNotificationInfo(
        title = title,
        message = message,
        time = createdAt?.let { formatCivicNotificationDate(it) } ?: "Just now",
        icon = icon,
        color = color,
        imageUrl = imageUrl
    )
}

@Preview(showBackground = true)
@Composable
fun OfficerNotificationScreenPreview() {
    CivicIssueTheme {
        OfficerNotificationScreen()
    }
}
