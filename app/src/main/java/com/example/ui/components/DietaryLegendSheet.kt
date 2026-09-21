package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeGlutenFreeBg
import com.example.ui.theme.BadgeGlutenFreeText
import com.example.ui.theme.BadgeKelaBg
import com.example.ui.theme.BadgeKelaText
import com.example.ui.theme.BadgeMilkFreeBg
import com.example.ui.theme.BadgeMilkFreeText
import com.example.ui.theme.BadgeVeganBg
import com.example.ui.theme.BadgeVeganText
import com.example.ui.theme.BrandBlue

data class DietaryLegendItem(
    val code: String,
    val title: String,
    val description: String,
    val badgeBg: Color,
    val badgeText: Color
)

val DIETARY_LEGEND_ITEMS = listOf(
    DietaryLegendItem(
        code = "Veg",
        title = "Vegan / Vegaaninen",
        description = "Plant-based dish prepared without any animal-derived ingredients.",
        badgeBg = BadgeVeganBg,
        badgeText = BadgeVeganText
    ),
    DietaryLegendItem(
        code = "G",
        title = "Gluten-Free / Gluteeniton",
        description = "Made without gluten-containing cereals (wheat, barley, rye, oats).",
        badgeBg = BadgeGlutenFreeBg,
        badgeText = BadgeGlutenFreeText
    ),
    DietaryLegendItem(
        code = "M",
        title = "Milk-Free / Maidoton",
        description = "Completely free of milk proteins and dairy lactose.",
        badgeBg = BadgeMilkFreeBg,
        badgeText = BadgeMilkFreeText
    ),
    DietaryLegendItem(
        code = "L",
        title = "Lactose-Free / Laktoositon",
        description = "Contains less than 0.01 g lactose per 100 g portion.",
        badgeBg = Color(0xFFEDE7F6),
        badgeText = Color(0xFF512DA8)
    ),
    DietaryLegendItem(
        code = "VL",
        title = "Low Lactose / Vähälaktoosinen",
        description = "Contains less than 1.0 g lactose per 100 g portion.",
        badgeBg = Color(0xFFF3E5F5),
        badgeText = Color(0xFF7B1FA2)
    ),
    DietaryLegendItem(
        code = "KELA",
        title = "KELA Student Subsidy / Kela-tuettu",
        description = "Meets the official nutritional requirements for the Finnish student meal subsidy (€3.10).",
        badgeBg = BadgeKelaBg,
        badgeText = BadgeKelaText
    ),
    DietaryLegendItem(
        code = "S",
        title = "Contains Pork / Sisältää sianlihaa",
        description = "Dish contains pork meat or pork-derived ingredients.",
        badgeBg = Color(0xFFFFEBEE),
        badgeText = Color(0xFFC62828)
    ),
    DietaryLegendItem(
        code = "CO₂e",
        title = "Climate Choice / Ilmastovalinta",
        description = "Calculated dish carbon footprint (kg CO₂e / portion) to help choose climate-friendly options.",
        badgeBg = Color(0xFFE0F2F1),
        badgeText = Color(0xFF00796B)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaryLegendSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("dietary_legend_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrandBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Dietary Labels Guide",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_legend_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Official UniCafe allergen and dietary classification badges:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(DIETARY_LEGEND_ITEMS, key = { it.code }) { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = item.badgeBg,
                                modifier = Modifier.size(width = 46.dp, height = 30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = item.code,
                                        color = item.badgeText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
