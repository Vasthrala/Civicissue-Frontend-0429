package com.simats.civicissue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(status: ComplaintStatus) {
    val color = when (status) {
        ComplaintStatus.RESOLVED, ComplaintStatus.COMPLETED -> Color(0xFF4CAF50)
        ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED -> Color(0xFF2196F3)
        ComplaintStatus.UNASSIGNED -> Color(0xFFFFA000)
        ComplaintStatus.REWORK -> Color(0xFFE53935)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PriorityBadge(priority: Priority) {
    Surface(
        color = priority.color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = priority.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = priority.color
        )
    }
}

@Composable
fun PriorityTag(priority: String) {
    val (bgColor, textColor) = when (priority.uppercase()) {
        "HIGH" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "MEDIUM" -> Color(0xFFFFF9C4) to Color(0xFFF9A825)
        else -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = priority,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// Custom Safe Click Modifier
fun Modifier.debouncedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: androidx.compose.ui.semantics.Role? = null,
    debounceTime: Long = 500L,
    onClick: () -> Unit
): Modifier = this.composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > debounceTime) {
                lastClickTime = currentTime
                onClick()
            }
        }
    )
}

/**
 * Global safe logger for debugging production-style issues
 */
fun safeLog(tag: String, message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        android.util.Log.e("CivicIssue_$tag", message, throwable)
    } else {
        android.util.Log.d("CivicIssue_$tag", message)
    }
}

// Navigation Helper
object SafeNavigator {
    private var lastNavTime = 0L
    private const val NAV_DEBOUNCE = 500L

    fun navigate(navController: androidx.navigation.NavController, route: String, options: androidx.navigation.NavOptionsBuilder.() -> Unit = {}) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavTime > NAV_DEBOUNCE) {
            lastNavTime = currentTime
            try {
                navController.navigate(route, options)
            } catch (e: Exception) {
                android.util.Log.e("SafeNavigator", "Navigation failed: $route", e)
            }
        }
    }
}
