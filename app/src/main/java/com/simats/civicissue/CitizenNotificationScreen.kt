package com.simats.civicissue

import android.graphics.Bitmap
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.CivicIssueTheme
import com.simats.civicissue.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenNotificationScreen(
    onBack: () -> Unit = {}
) {
    var selectedNotification by remember { mutableStateOf<CitizenNotificationInfo?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(CitizenNotificationStore.citizenNotifications) { notification ->
                CitizenNotificationItem(notification = notification) {
                    selectedNotification = notification
                    showSheet = true
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
                selectedNotification?.let { NotificationDetailContent(it) }
            }
        }
    }
}

@Composable
fun CitizenNotificationItem(
    notification: CitizenNotificationInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(notification.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = notification.time,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun NotificationDetailContent(notification: CitizenNotificationInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(notification.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = notification.icon,
                contentDescription = null,
                tint = notification.color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = notification.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = notification.time,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = notification.message,
            fontSize = 15.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        if (notification.image != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Attached Proof:",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                bitmap = notification.image.asImageBitmap(),
                contentDescription = "Resolution Proof",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* Could mark as read or navigate */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Acknowledge", fontWeight = FontWeight.Bold)
        }
    }
}

data class CitizenNotificationInfo(
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    val image: Bitmap? = null
)

object CitizenNotificationStore {
    val citizenNotifications = mutableStateListOf(
        CitizenNotificationInfo(
            "Work Completed!",
            "The issue #CE-112 at Jubilee Hills has been resolved. [Completion Photo Attached]",
            "Just now",
            Icons.Default.TaskAlt,
            Color(0xFF4CAF50)
        ),
        CitizenNotificationInfo(
            "Issue Reported Successfully",
            "Your report about 'Pothole on Main St.' (#CE-112) has been successfully submitted.",
            "Just now",
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50)
        ),
        CitizenNotificationInfo(
            "Status Update",
            "The status of your report #CE-108 has been changed to 'In Progress'.",
            "2h ago",
            Icons.Default.Refresh,
            PrimaryBlue
        ),
        CitizenNotificationInfo(
            "Official Response",
            "An official has added a comment to your report regarding street lights.",
            "5h ago",
            Icons.Default.Comment,
            Color(0xFFFFA000)
        ),
        CitizenNotificationInfo(
            "Maintenance Alert",
            "Planned maintenance in your area will cause water supply cuts tomorrow.",
            "1d ago",
            Icons.Default.Warning,
            Color(0xFFD32F2F)
        ),
        CitizenNotificationInfo(
            "Congratulations!",
            "Thank you for being an active citizen! Your 10th report was resolved.",
            "2d ago",
            Icons.Default.Stars,
            Color(0xFF9C27B0)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CitizenNotificationScreenPreview() {
    CivicIssueTheme {
        CitizenNotificationScreen()
    }
}
