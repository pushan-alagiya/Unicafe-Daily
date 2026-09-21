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
import com.example.domain.model.StatusState
import com.example.ui.theme.StatusClosedRedBg
import com.example.ui.theme.StatusClosedRedBgDark
import com.example.ui.theme.StatusClosedRedText
import com.example.ui.theme.StatusClosedRedTextDark
import com.example.ui.theme.StatusOpenGreenBg
import com.example.ui.theme.StatusOpenGreenBgDark
import com.example.ui.theme.StatusOpenGreenText
import com.example.ui.theme.StatusOpenGreenTextDark
import com.example.ui.theme.StatusOpeningSoonBg
import com.example.ui.theme.StatusOpeningSoonBgDark
import com.example.ui.theme.StatusOpeningSoonText
import com.example.ui.theme.StatusOpeningSoonTextDark

@Composable
fun StatusBadge(
    status: RestaurantStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, dotColor, textColor) = when (status.state) {
        StatusState.OPEN -> Triple(
            if (isDark) StatusOpenGreenBgDark else StatusOpenGreenBg,
            Color(0xFF10B981),
            if (isDark) StatusOpenGreenTextDark else StatusOpenGreenText
        )
        StatusState.OPENING_SOON -> Triple(
            if (isDark) StatusOpeningSoonBgDark else StatusOpeningSoonBg,
            Color(0xFFF59E0B),
            if (isDark) StatusOpeningSoonTextDark else StatusOpeningSoonText
        )
        StatusState.CLOSED -> Triple(
            if (isDark) StatusClosedRedBgDark else StatusClosedRedBg,
            Color(0xFFEF4444),
            if (isDark) StatusClosedRedTextDark else StatusClosedRedText
        )
    }

    val countdown = status.countdownText
    val isClosedToday = status.state == StatusState.CLOSED && (status.hoursDescription.contains("closed today", ignoreCase = true) ||
            status.countdownText?.contains("closed today", ignoreCase = true) == true)

    val badgeLabel = when (status.state) {
        StatusState.OPEN -> if (!countdown.isNullOrBlank()) "OPEN · $countdown" else "OPEN"
        StatusState.OPENING_SOON -> if (!countdown.isNullOrBlank()) "OPENING SOON · $countdown" else "OPENING SOON"
        StatusState.CLOSED -> if (isClosedToday) "CLOSED TODAY" else "CLOSED"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .testTag("status_badge_${status.state.name.lowercase()}")
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

