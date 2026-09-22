package fi.pushan.unicafedaily.ui.components

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
import fi.pushan.unicafedaily.domain.model.CustomerCategory
import fi.pushan.unicafedaily.ui.theme.BadgeAllergenBg
import fi.pushan.unicafedaily.ui.theme.BadgeAllergenText
import fi.pushan.unicafedaily.ui.theme.BadgeGlutenFreeBg
import fi.pushan.unicafedaily.ui.theme.BadgeGlutenFreeText
import fi.pushan.unicafedaily.ui.theme.BadgeMilkFreeBg
import fi.pushan.unicafedaily.ui.theme.BadgeMilkFreeText
import fi.pushan.unicafedaily.ui.theme.BadgeVeganBg
import fi.pushan.unicafedaily.ui.theme.BadgeVeganText
import fi.pushan.unicafedaily.ui.theme.BrandBlue

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
    selectedCategory: CustomerCategory = CustomerCategory.STUDENT,
    onCategorySelected: (CustomerCategory) -> Unit = {},
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
            PriceRow("Standard Lunch", "€3.10", "€6.35", "€7.30", "€9.80"),
            PriceRow("Today's Special", "€5.30", "€8.75", "€8.90", "€11.50"),
            PriceRow("Buffet", "€10.50", "€11.00", "€11.00", "€13.00"),
            PriceRow("Breakfast", "€4.00", "€4.00", "€4.50", "€4.50")
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Active Rate: ${selectedCategory.displayName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (selectedCategory) {
                                            CustomerCategory.STUDENT -> "Subsidized by Kela with valid Finnish student card"
                                            CustomerCategory.GRADUATE -> "Post-graduate rate with valid student union card"
                                            CustomerCategory.STAFF -> "University of Helsinki staff and pensioners"
                                            CustomerCategory.NORMAL -> "Open to all visitors, visitors & public"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Price Overview Table
                        Text(
                            text = "PRICE LIST COMPARISON",
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
                                    val price = when (selectedCategory) {
                                        CustomerCategory.STUDENT -> row.student
                                        CustomerCategory.GRADUATE -> row.postGrad
                                        CustomerCategory.STAFF -> row.staff
                                        CustomerCategory.NORMAL -> row.normal
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = row.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Student: ${row.student} • Normal: ${row.normal}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = price,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (idx < standardPriceRows.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Kela Subsidy Note
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Prices are regulated by the Finnish Social Insurance Institution (Kela). Student lunch includes a warm main dish, salad buffet, bread & spread, and a beverage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    1 -> {
                        // DIET SYMBOLS
                        Text(
                            text = "DIETARY MARKINGS & BADGES",
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
                                dietSymbols.forEachIndexed { idx, symbol ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = symbol.bgColor,
                                            modifier = Modifier.width(52.dp)
                                        ) {
                                            Text(
                                                text = symbol.code,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = symbol.textColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = symbol.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = symbol.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    if (idx < dietSymbols.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ALLERGENS
                        Text(
                            text = "EU FOOD ALLERGEN REGULATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UniCafe clearly marks all major food allergens per EU Regulation (EU) No 1169/2011.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                allergensList.forEachIndexed { idx, (allergen, examples) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = allergen,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = examples,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (idx < allergensList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
