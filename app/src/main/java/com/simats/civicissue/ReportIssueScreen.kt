package com.simats.civicissue

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.CivicIssueTheme
import com.simats.civicissue.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    onBack: () -> Unit = {},
    onViewComplaints: () -> Unit = {}
) {
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // AI States
    var isDetectingAI by remember { mutableStateOf(false) }
    var showSeverityPopup by remember { mutableStateOf(false) }
    var detectedSeverity by remember { mutableStateOf("High") }
    
    // Map State
    var showMapPicker by remember { mutableStateOf(false) }

    val categories = listOf("Pothole", "Street Light", "Waste Collection", "Water Leakage", "Drainage", "Other")

    val scope = rememberCoroutineScope()
    
    fun simulateAIDetection() {
        isDetectingAI = true
        scope.launch {
            delay(2000) // Simulate 2 seconds of analysis
            isDetectingAI = false
            showSeverityPopup = true
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            simulateAIDetection()
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
            simulateAIDetection()
        }
    }

    if (isSubmitted) {
        SuccessView(
            onBackToHome = onBack,
            onTrackStatus = onViewComplaints
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Report Issue",
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
            bottomBar = {
                Surface(tonalElevation = 8.dp, color = Color.White) {
                    NavigationBar(
                        containerColor = Color.White,
                        modifier = Modifier.height(72.dp)
                    ) {
                        NavigationBarItem(
                            selected = false,
                            onClick = onBack,
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray
                            )
                        )
                        NavigationBarItem(
                            selected = true,
                            onClick = { },
                            icon = { Icon(Icons.Default.Warning, contentDescription = "Report") },
                            label = { Text("Report", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = onViewComplaints,
                            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Complaints") },
                            label = { Text("Complaints", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray
                            )
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { },
                            icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray
                            )
                        )
                    }
                }
            },
            containerColor = Color(0xFFF8F9FA)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Photo Upload Area
                PhotoUploadSection(
                    capturedBitmap = capturedBitmap,
                    selectedImageUri = selectedImageUri,
                    onCameraClick = { cameraLauncher.launch() },
                    onGalleryClick = { galleryLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Location Input
                ReportLabel("Location")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Fetching location...", color = Color.DarkGray) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Open Map",
                            tint = PrimaryBlue,
                            modifier = Modifier.clickable { showMapPicker = true }
                        )
                    },
                    trailingIcon = {
                        Text(
                            "Locate Me",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { location = "Near CMR School, Medchal, Hyderabad" },
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category Dropdown
                ReportLabel("Category")
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory.ifEmpty { "Select issue category" },
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Category, contentDescription = null, tint = Color.DarkGray)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryBlue,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedPlaceholderColor = Color.DarkGray,
                            focusedPlaceholderColor = Color.DarkGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, color = Color.Black) },
                                onClick = {
                                    selectedCategory = selectionOption
                                    categoryExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description Input
                ReportLabel("Description")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = {
                        Text(
                            "Describe the issue in detail (e.g. depth of pothole, exact pole number)...",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = { isSubmitted = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Submit Complaint", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // AI Processing Overlay
    if (isDetectingAI) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.size(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI Analyzing...", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Detecting Severity", fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }

    // AI Severity Popup
    if (showSeverityPopup) {
        AlertDialog(
            onDismissRequest = { showSeverityPopup = false },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF6200EE)) },
            title = { Text("AI Insight", color = Color.Black) },
            text = {
                Column {
                    Text("Based on the photo, AI has detected a potential issue.", color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (detectedSeverity == "High") Color(0xFFFFEBEE) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (detectedSeverity == "High") Icons.Default.PriorityHigh else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (detectedSeverity == "High") Color.Red else Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Detected Severity: $detectedSeverity",
                                fontWeight = FontWeight.Bold,
                                color = if (detectedSeverity == "High") Color.Red else Color(0xFFE65100),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSeverityPopup = false }) {
                    Text("Confirm")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Map Picker Dialog
    if (showMapPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showMapPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFE3F2FD))) {
                        // Mock Map background
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Text("Mock Map View", color = PrimaryBlue)
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Pin Location", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("12-4/A, Jubilee Hills Rd No 36, Hyderabad", fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                location = "12-4/A, Jubilee Hills Rd No 36, Hyderabad"
                                showMapPicker = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Tag This Location")
                        }
                        TextButton(onClick = { showMapPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel", color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessView(
    onBackToHome: () -> Unit,
    onTrackStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Complaint Submitted Successfully!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Text(
            "Your report #CE-112 has been registered. You can track its progress below.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Status Timeline
        Text(
            "Status Tracking",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StatusTimelineItem("Submitted", "Today, 10:40 AM", true, true)
        StatusTimelineItem("Technical Review", "Pending", false, true)
        StatusTimelineItem("Officer Assigned", "Pending", false, true)
        StatusTimelineItem("Resolved", "Waiting", false, false)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusTimelineItem(title: String, subtitle: String, isDone: Boolean, hasNext: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Color(0xFF4CAF50) else Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            if (hasNext) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(if (isDone) Color(0xFF4CAF50) else Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = if (isDone) Color.Black else Color.DarkGray)
            Text(subtitle, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun PhotoUploadSection(
    capturedBitmap: Bitmap?,
    selectedImageUri: Uri?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 1.dp,
                color = PrimaryBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryBlue.copy(alpha = 0.02f))
            .clickable { showOptions = true },
        contentAlignment = Alignment.Center
    ) {
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (selectedImageUri != null) {
            // In a real app, you'd use Coil or Glide here to load the URI
            // Since I can't add external libs easily in this snippet without Coil, I'll show a "Photo Selected" text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Photo, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                Text("Photo Selected from Gallery", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Upload Photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    "PNG, JPG up to 10MB",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Add Photo", color = Color.Black) },
            text = { Text("Choose an option to add a photo of the issue.", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    onCameraClick()
                    showOptions = false
                }) {
                    Text("Camera", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onGalleryClick()
                    showOptions = false
                }) {
                    Text("Choose File", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ReportLabel(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Preview(showBackground = true)
@Composable
fun ReportIssueScreenPreview() {
    CivicIssueTheme {
        ReportIssueScreen()
    }
}
