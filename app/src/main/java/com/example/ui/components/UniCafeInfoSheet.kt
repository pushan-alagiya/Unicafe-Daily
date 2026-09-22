package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CustomerCategory
import com.example.ui.theme.BadgeAllergenBg
import com.example.ui.theme.BadgeAllergenText
import com.example.ui.theme.BadgeGlutenFreeBg
import com.example.ui.theme.BadgeGlutenFreeText
import com.example.ui.theme.BadgeMilkFreeBg
import com.example.ui.theme.BadgeMilkFreeText
import com.example.ui.theme.BadgeVeganBg
import com.example.ui.theme.BadgeVeganText
import com.example.ui.theme.BrandBlue

private data class DietSymbol(
    val code: String,
    val name: String,
    val description: String,
    val bgColor: Color,
    val textColor: Color
)

private data class PriceRow(
    val title: String,
    val student: String,
    val postGrad: String,
    val staff: String,
    val normal: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UniCafeInfoSheet(
    selectedCategory: CustomerCategory,
    onCategorySelected: (CustomerCategory) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val dietSymbols = remember {
        listOf(
            DietSymbol("Veg", "Vegan", "Contains no animal-derived ingredients or byproducts", BadgeVeganBg, BadgeVeganText),
            DietSymbol("G", "Gluten Free", "Contains less than 20 mg/kg gluten (safe for coeliacs)", BadgeGlutenFreeBg, BadgeGlutenFreeText),
            DietSymbol("VL", "Low Lactose", "Contains less than 1 g lactose per 100 g", BadgeMilkFreeBg, BadgeMilkFreeText),
            DietSymbol("L", "Lactose Free", "Contains less than 0.01 g lactose per 100 g", BadgeMilkFreeBg, BadgeMilkFreeText),
            DietSymbol("M", "Milk Free", "Contains no dairy, milk protein, or lactose", BadgeMilkFreeBg, BadgeMilkFreeText),
            DietSymbol("KELA", "Kela's Recommendations", "Fulfills Kela meal subsidy nutritional criteria for university students", Color(0xFFE0E7FF), Color(0xFF3730A3))
        )
    }

    val allergensList = remember {
        listOf(
            "Cereals containing gluten" to "Wheat, rye, barley, oats, and products made from them",
            "Crustaceans & Seafood" to "Crustaceans, prawns, crabs, and products made from them",
            "Eggs" to "Eggs and egg products",
            "Fish" to "Fish and fish products",
            "Peanuts" to "Peanuts and peanut products",
            "Soybeans" to "Soybeans and soy products (tofu, edamame, lecithin)",
            "Milk & Dairy" to "Milk, milk products, whey, and lactose",
            "Tree Nuts" to "Almonds, hazelnuts, walnuts, cashews, pecans, Brazil nuts, pistachios, macadamias",
            "Celery" to "Celery, celeriac, and products made from them",
            "Mustard" to "Mustard seeds, powder, and products made from them",
            "Sesame Seeds" to "Sesame seeds, tahini, and sesame products",
            "Sulfur Dioxide & Sulfites" to "Concentrations higher than 10 mg/kg or 10 mg/l (wine, vinegar, dried fruit)",
            "Lupine" to "Lupine and lupine products",
            "Molluscs" to "Mussels, clams, oysters, squid, octopus, snails"
        )
    }

    val standardPriceRows = remember {
        listOf(
            PriceRow("Lunch", "€3.10", "€6.35", "€7.30", "€9.80"),
            PriceRow("Today's Special", "€5.30", "€8.75", "€8.90", "€11.50"),
            PriceRow("Buffet", "€10.50", "€11.00", "€11.00", "€13.00"),
            PriceRow("Breakfast (Biokeskus & Porthania)", "€4.00", "€4.00", "€4.50", "€4.50")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("unicafe_info_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UniCafe Guide & Rates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_info_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tabs: 0: Pricing & Category, 1: Diet Symbols, 2: Allergens
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Prices & Rates", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Diet Symbols", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Allergens", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // PRICING & CATEGORY
                        Text(
                            text = "SELECT YOUR CUSTOMER RATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CustomerCategory.entries.forEach { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onCategorySelected(category) },
                                    label = {
                                        Text(
                                            text = category.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Rate Notice
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Prices across the app are customized for: ${selectedCategory.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // OFFICIAL PRICE LIST TABLE
                        Text(
                            text = "OFFICIAL PRICE LIST (1.1.2026)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                standardPriceRows.forEachIndexed { idx, row ->
                                    Column {
                                        Text(
                                            text = row.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            PriceChip(label = "Student", price = row.student, isHighlighted = selectedCategory == CustomerCategory.STUDENT)
                                            PriceChip(label = "Post-grad", price = row.postGrad, isHighlighted = selectedCategory == CustomerCategory.GRADUATE)
                                            PriceChip(label = "Staff", price = row.staff, isHighlighted = selectedCategory == CustomerCategory.STAFF)
                                            PriceChip(label = "Normal", price = row.normal, isHighlighted = selectedCategory == CustomerCategory.NORMAL)
                                        }
                                    }
                                    if (idx < standardPriceRows.lastIndex) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // LEFTOVER LUNCH & EXTRAS
                        Text(
                            text = "LEFTOVER LUNCH & EXTRAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Leftover lunch (UniCafe container)", style = MaterialTheme.typography.bodyMedium)
                                    Text("€3.00", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Leftover lunch (Own container)", style = MaterialTheme.typography.bodyMedium)
                                    Text("€2.70", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Side salad", style = MaterialTheme.typography.bodyMedium)
                                    Text("€1.00", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Energy supplement", style = MaterialTheme.typography.bodyMedium)
                                    Text("€0.70", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Child lunch (0–12 yrs, Lunch / Special)", style = MaterialTheme.typography.bodyMedium)
                                    Text("€5.85 / €6.85", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Additional scoop (Lunch / Special)", style = MaterialTheme.typography.bodyMedium)
                                    Text("€1.80 / €3.05", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    1 -> {
                        // DIET SYMBOLS
                        Text(
                            text = "SPECIAL DIET SYMBOLS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        dietSymbols.forEach { sym ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = sym.bgColor,
                                        modifier = Modifier.size(width = 54.dp, height = 36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = sym.code,
                                                color = sym.textColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = sym.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sym.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ALLERGENS
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "If you have severe or life-threatening food allergies, please always verify recipe ingredients directly with our restaurant staff on-site.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "14 COMMON EU & FINNISH ALLERGENS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                allergensList.forEachIndexed { idx, item ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "${idx + 1}. ${item.first}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.second,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (idx < allergensList.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceChip(
    label: String,
    price: String,
    isHighlighted: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlighted) BrandBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = price,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
