package com.simats.civicissue

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simats.civicissue.ui.theme.BackgroundBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAdminDashboardScreen(
    onComplaintClick: (String) -> Unit,
    onBack: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modern Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlue,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = BackgroundBlue
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Text("Professional admin dashboard features will appear here.", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onReportsClick) {
                Text("View All Reports")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onProfileClick) {
                Text("My Profile")
            }
        }
    }
}
