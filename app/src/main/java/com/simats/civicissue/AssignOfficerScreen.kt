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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun AssignOfficerScreen(
    complaintId: String,
    onBack: () -> Unit,
    onAssignComplete: (Officer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val mockOfficers = remember {
        listOf(
            Officer("OFF-001", "Officer Rajesh", "Road Maintenance", 3),
            Officer("OFF-002", "Officer Sneha", "Welfare & Sanitation", 1),
            Officer("OFF-003", "Officer Vikram", "Electricity Board", 5),
            Officer("OFF-004", "Officer Anjali", "Water Management", 2),
            Officer("OFF-005", "Officer Karthik", "Road Maintenance", 0)
        )
    }

    val filteredOfficers = mockOfficers.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.department.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Officer", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Assigning for Ticket:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(complaintId, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search Officer or Department...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "Available Officers",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOfficers) { officer ->
                    OfficerItem(officer = officer, onClick = { onAssignComplete(officer) })
                }
            }
        }
    }
}

@Composable
fun OfficerItem(officer: Officer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    officer.name.take(1),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(officer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(officer.department, color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Workload", fontSize = 10.sp, color = Color.Gray)
                Text("${officer.workloadCount} Active", fontWeight = FontWeight.Bold, color = if (officer.workloadCount > 3) Color.Red else Color(0xFF4CAF50))
            }
        }
    }
}
