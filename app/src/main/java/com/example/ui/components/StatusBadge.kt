package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RestaurantStatus
import com.example.ui.theme.StatusClosedRed
import com.example.ui.theme.StatusClosedRedBg
import com.example.ui.theme.StatusClosedRedBgDark
import com.example.ui.theme.StatusClosedRedText
import com.example.ui.theme.StatusClosedRedTextDark
import com.example.ui.theme.StatusOpenGreen
import com.example.ui.theme.StatusOpenGreenBg
import com.example.ui.theme.StatusOpenGreenBgDark
import com.example.ui.theme.StatusOpenGreenText
import com.example.ui.theme.StatusOpenGreenTextDark

@Composable
fun StatusBadge(
    status: RestaurantStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isOpen = status.isOpenNow

    val bgColor = if (isOpen) {
        if (isDark) StatusOpenGreenBgDark else StatusOpenGreenBg
    } else {
        if (isDark) StatusClosedRedBgDark else StatusClosedRedBg
    }

    val dotColor = if (isOpen) {
        Color(0xFF10B981)
    } else {
        Color(0xFFEF4444)
    }

    val textColor = if (isOpen) {
        if (isDark) StatusOpenGreenTextDark else StatusOpenGreenText
    } else {
        if (isDark) StatusClosedRedTextDark else StatusClosedRedText
    }

    val countdown = status.countdownText
    val isClosedToday = !isOpen && (status.hoursDescription.contains("closed today", ignoreCase = true) ||
            status.countdownText?.contains("closed today", ignoreCase = true) == true)
    val badgeLabel = when {
        isOpen && !countdown.isNullOrBlank() -> "OPEN · $countdown"
        isOpen -> "OPEN"
        isClosedToday -> "CLOSED TODAY"
        !countdown.isNullOrBlank() -> "CLOSED · $countdown"
        else -> "CLOSED"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .testTag("status_badge_${if (isOpen) "open" else "closed"}")
            .semantics { contentDescription = "Restaurant status: $badgeLabel, ${status.hoursDescription}" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = badgeLabel,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

