package com.simats.civicissue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.civicissue.ui.theme.PrimaryBlue
import com.simats.civicissue.ui.theme.BackgroundBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAIChatbotScreen(
    onBack: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { 
        mutableStateListOf(
            ChatMessage(text = "Hello Admin! I'm your AI Management Assistant. How can I help you manage civic issues today?", isUser = false),
            ChatMessage(text = "You can manage complaints, assign officers, or view system analytics for better decision making.", isUser = false)
        )
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isVoiceActive by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            messages.add(ChatMessage(text = "[Document/Image Attached]", isUser = true))
            coroutineScope.launch {
                delay(1000)
                messages.add(ChatMessage(text = "I've received the file. Analyzing documentation for administrative relevance...", isUser = false))
            }
        }
    }

    val quickActions = listOf(
        "Manage Complaints" to Icons.Default.Assignment,
        "Assign Officers" to Icons.Default.PersonAdd,
        "View Analytics" to Icons.Default.BarChart,
        "System Logs" to Icons.Default.ReceiptLong,
        "Check Pending" to Icons.Default.PendingActions
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Admin AI Assistant", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Management Intelligence", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
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
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = BackgroundBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }

            // Bottom Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Quick Actions
                Text(
                    "Quick Management Actions",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(quickActions) { action ->
                        SuggestionChip(label = action.first, icon = action.second) {
                            inputText = action.first
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { imageLauncher.launch("application/*,image/*") }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.DarkGray)
                    }
                    IconButton(onClick = {
                        isVoiceActive = true
                        coroutineScope.launch {
                            delay(2000)
                            isVoiceActive = false
                            val mockVoiceText = "Show me urgent pending tasks"
                            messages.add(ChatMessage(text = mockVoiceText, isUser = true))
                            delay(1000)
                            messages.add(ChatMessage(text = "Retrieving urgent pending reports from all departments...", isUser = false))
                        }
                    }) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = if (isVoiceActive) PrimaryBlue else Color.DarkGray
                        )
                    }
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about management...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedPlaceholderColor = Color.DarkGray,
                            focusedPlaceholderColor = Color.DarkGray
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    val currentInput = inputText
                                    messages.add(ChatMessage(text = currentInput, isUser = true))
                                    inputText = ""
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                        delay(1000)
                                        val response = getAdminBotResponse(currentInput)
                                        messages.add(ChatMessage(text = response, isUser = false))
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            },
                        color = PrimaryBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun getAdminBotResponse(input: String): String {
    val lowerInput = input.lowercase()
    return when {
        lowerInput.contains("complaint") || lowerInput.contains("report") -> "You can manage all complaints in the 'Reports' section. Should I show you the most urgent ones filtered by severity?"
        lowerInput.contains("assign") || lowerInput.contains("officer") -> "To assign an officer, go to the complaint details and click 'Assign Officer'. I can help you find available officers in that specific department."
        lowerInput.contains("analytic") || lowerInput.contains("data") || lowerInput.contains("chart") -> "The analytics dashboard provides real-time data on issue resolution rates, department performance, and citizen satisfaction scores."
        lowerInput.contains("log") || lowerInput.contains("history") -> "System logs and administrative history are available in the Settings menu. They track all actions and system events for auditing."
        lowerInput.contains("pending") || lowerInput.contains("todo") -> "I've checked the database. There are currently 12 assigned complaints and 5 in progress. You might want to follow up on #CE-102."
        lowerInput.contains("thank") -> "Always at your service, Admin. Let's keep the city running smoothly!"
        else -> "I understand you're asking about '$input'. As your Admin Assistant, I can help with report management, officer assignment, and system oversight. How should we proceed?"
    }
}
