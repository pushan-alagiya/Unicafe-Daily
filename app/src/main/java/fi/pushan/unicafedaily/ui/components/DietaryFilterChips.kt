package fi.pushan.unicafedaily.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.ui.theme.BrandBlue

@Composable
fun DietaryFilterChips(
    selectedFilter: DietaryFilter,
    onFilterSelected: (DietaryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DietaryFilter.values().forEach { filter ->
            val isSelected = filter == selectedFilter

            val (icon: ImageVector, title: String, fullDesc: String) = when (filter) {
                DietaryFilter.ALL -> Triple(Icons.Default.RestaurantMenu, "All", "All items")
                DietaryFilter.VEG -> Triple(Icons.Default.Spa, "Veg", "Vegetarian and Vegan")
                DietaryFilter.GLUTEN_FREE -> Triple(Icons.Default.Grass, "G", "Gluten-free")
                DietaryFilter.MILK_FREE -> Triple(Icons.Default.Opacity, "M", "Milk-free / Lactose-free")
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surface,
                label = "chip_bg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                label = "chip_content"
            )
            val borderColor = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(24.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                shadowElevation = if (isSelected) 3.dp else 0.dp,
                modifier = Modifier
                    .height(42.dp)
                    .testTag("filter_chip_${filter.name.lowercase()}")
                    .semantics {
                        contentDescription = "Filter by $fullDesc. ${if (isSelected) "Selected" else "Not selected"}"
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
