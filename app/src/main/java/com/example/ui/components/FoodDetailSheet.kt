package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Meal
import com.example.domain.model.MealType
import com.example.domain.model.NutritionInfo
import com.example.ui.theme.BadgeAllergenBg
import com.example.ui.theme.BadgeAllergenText
import com.example.ui.theme.BadgeClimateBg
import com.example.ui.theme.BadgeClimateText
import com.example.ui.theme.BadgeGlutenFreeBg
import com.example.ui.theme.BadgeGlutenFreeText
import com.example.ui.theme.BadgeMilkFreeBg
import com.example.ui.theme.BadgeMilkFreeText
import com.example.ui.theme.BadgeVeganBg
import com.example.ui.theme.BadgeVeganText
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CalorieCardBg
import com.example.ui.theme.CalorieCardText
import com.example.ui.theme.CarbsCardBg
import com.example.ui.theme.CarbsCardText
import com.example.ui.theme.FatCardBg
import com.example.ui.theme.FatCardText
import com.example.ui.theme.FiberCardBg
import com.example.ui.theme.FiberCardText
import com.example.ui.theme.ProteinCardBg
import com.example.ui.theme.ProteinCardText

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestaurantMenu
import com.example.ui.theme.BadgeAllergenBgDark
import com.example.ui.theme.BadgeAllergenTextDark
import com.example.ui.theme.BadgeClimateBgDark
import com.example.ui.theme.BadgeClimateTextDark
import com.example.ui.theme.BadgeGlutenFreeBgDark
import com.example.ui.theme.BadgeGlutenFreeTextDark
import com.example.ui.theme.BadgeMilkFreeBgDark
import com.example.ui.theme.BadgeMilkFreeTextDark
import com.example.ui.theme.BadgeVeganBgDark
import com.example.ui.theme.BadgeVeganTextDark
import com.example.ui.theme.CalorieCardBgDark
import com.example.ui.theme.CalorieCardTextDark
import com.example.ui.theme.CarbsCardBgDark
import com.example.ui.theme.CarbsCardTextDark
import com.example.ui.theme.FatCardBgDark
import com.example.ui.theme.FatCardTextDark
import com.example.ui.theme.FiberCardBgDark
import com.example.ui.theme.FiberCardTextDark
import com.example.ui.theme.ProteinCardBgDark
import com.example.ui.theme.ProteinCardTextDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodDetailSheet(
    meal: Meal,
    restaurantName: String,
    isFavoriteMeal: Boolean = false,
    onToggleFavoriteMeal: (() -> Unit)? = null,
    isEatenToday: Boolean = false,
    onRecordEaten: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = isSystemInDarkTheme()
    var hasEatenRecorded by remember(isEatenToday) { mutableStateOf(isEatenToday) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("food_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Top Bar: Category Pill + Favorite, Share & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = getMealTypeIcon(meal.mealType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = meal.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star favourite meal
                    if (onToggleFavoriteMeal != null) {
                        IconButton(
                            onClick = onToggleFavoriteMeal,
                            modifier = Modifier.testTag("toggle_favorite_meal_button")
                        ) {
                            Icon(
                                imageVector = if (isFavoriteMeal) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavoriteMeal) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavoriteMeal) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { shareMeal(context, meal, restaurantName) },
                        modifier = Modifier.testTag("share_meal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Meal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meal Title & Favorite badge if starred
            if (isFavoriteMeal) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Starred Wishlist Dish",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Text(
                text = meal.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Restaurant & Price Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Served at $restaurantName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    meal.studentPrice?.let { price ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandBlue,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Student $price",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    meal.normalPrice?.let { price ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Normal $price",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dietary Badges Row
            if (meal.dietaryBadges.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    meal.dietaryBadges.forEach { badge ->
                        DetailedDietaryChip(badge = badge, isDark = isDark)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Macro Nutrition Dashboard (Calories, Protein, Carbs, Fat, etc.)
            Text(
                text = "NUTRITIONAL VALUES (PER PORTION)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val nutrition = meal.nutritionInfo
            if (nutrition != null && nutrition.hasMacros) {
                MacroNutritionGrid(nutrition = nutrition, isDark = isDark)
            } else if (!meal.nutrition.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = meal.nutrition,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Detailed macro breakdown not provided by UniCafe for this meal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Carbon Footprint Card
            meal.carbonFootprint?.let { co2 ->
                Spacer(modifier = Modifier.height(16.dp))
                CarbonFootprintCard(co2Text = co2, isDark = isDark)
            }

            // Allergens Section
            if (meal.allergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                val allergenBg = if (isDark) BadgeAllergenBgDark else BadgeAllergenBg
                val allergenText = if (isDark) BadgeAllergenTextDark else BadgeAllergenText
                Text(
                    text = "ALLERGENS & INTOLERANCES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = allergenText,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    meal.allergens.forEach { allergen ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(allergenBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = allergenText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = allergen,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = allergenText
                            )
                        }
                    }
                }
            }

            // Ingredients Section - Clean, Continuous Ingredients Card
            val fullIngredients = meal.ingredients
            if (!fullIngredients.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                IngredientsCard(
                    ingredientsText = fullIngredients,
                    copyCallback = {
                        clipboardManager.setText(AnnotatedString(fullIngredients))
                        Toast.makeText(context, "Ingredients copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: "I ate this 😋" + "Done"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onRecordEaten != null) {
                    OutlinedButton(
                        onClick = {
                            onRecordEaten()
                            hasEatenRecorded = true
                            Toast.makeText(context, "Added to lunch tracker! 😋", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("record_eaten_button")
                    ) {
                        Icon(
                            imageVector = if (hasEatenRecorded) Icons.Default.Check else Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = if (hasEatenRecorded) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasEatenRecorded) "Logged!" else "I ate this 😋",
                            fontWeight = FontWeight.Bold,
                            color = if (hasEatenRecorded) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("detail_done_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IngredientsCard(
    ingredientsText: String,
    copyCallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INGREDIENTS / AINESOSAT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { copyCallback() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ingredientsText.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MacroNutritionGrid(nutrition: NutritionInfo, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Calories & Protein
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val calBg = if (isDark) CalorieCardBgDark else CalorieCardBg
            val calText = if (isDark) CalorieCardTextDark else CalorieCardText
            val protBg = if (isDark) ProteinCardBgDark else ProteinCardBg
            val protText = if (isDark) ProteinCardTextDark else ProteinCardText

            MacroCard(
                icon = Icons.Default.LocalFireDepartment,
                value = nutrition.caloriesKcal ?: "N/A",
                label = "Calories",
                subtext = nutrition.energyKj,
                bgColor = calBg,
                textColor = calText,
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                icon = Icons.Default.FitnessCenter,
                value = nutrition.protein ?: "N/A",
                label = "Protein",
                subtext = "Muscle fuel",
                bgColor = protBg,
                textColor = protText,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Carbs & Fat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val carbBg = if (isDark) CarbsCardBgDark else CarbsCardBg
            val carbText = if (isDark) CarbsCardTextDark else CarbsCardText
            val fatBg = if (isDark) FatCardBgDark else FatCardBg
            val fatText = if (isDark) FatCardTextDark else FatCardText

            MacroCard(
                icon = Icons.Default.Grass,
                value = nutrition.carbs ?: "N/A",
                label = "Carbs",
                subtext = nutrition.sugars?.let { "Sugars $it" },
                bgColor = carbBg,
                textColor = carbText,
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                icon = Icons.Default.Opacity,
                value = nutrition.fat ?: "N/A",
                label = "Fat",
                subtext = nutrition.saturatedFat?.let { "Sat. $it" },
                bgColor = fatBg,
                textColor = fatText,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Fiber & Salt (if available)
        if (nutrition.fiber != null || nutrition.salt != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nutrition.fiber?.let { fib ->
                    val fibBg = if (isDark) FiberCardBgDark else FiberCardBg
                    val fibText = if (isDark) FiberCardTextDark else FiberCardText
                    MacroCard(
                        icon = Icons.Default.Spa,
                        value = fib,
                        label = "Fiber",
                        subtext = "Dietary fiber",
                        bgColor = fibBg,
                        textColor = fibText,
                        modifier = Modifier.weight(1f)
                    )
                }
                nutrition.salt?.let { salt ->
                    MacroCard(
                        icon = Icons.Outlined.Egg,
                        value = salt,
                        label = "Salt",
                        subtext = nutrition.lactose?.let { "Lactose $it" } ?: "Sodium",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroCard(
    icon: ImageVector,
    value: String,
    label: String,
    subtext: String?,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.85f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CarbonFootprintCard(co2Text: String, isDark: Boolean) {
    val climateBg = if (isDark) BadgeClimateBgDark else BadgeClimateBg
    val climateText = if (isDark) BadgeClimateTextDark else BadgeClimateText
    val borderColor = if (isDark) Color(0xFF065F46) else Color(0xFFA7F3D0)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = climateBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = climateText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Carbon Footprint",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = climateText
                )
                Text(
                    text = co2Text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF064E3B)
                )
                Text(
                    text = "Calculated per student portion according to UniCafe climate rating.",
                    style = MaterialTheme.typography.labelSmall,
                    color = climateText.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DetailedDietaryChip(badge: String, isDark: Boolean = false, modifier: Modifier = Modifier) {
    val (bgColor, textColor, desc) = when {
        badge.equals("Veg", ignoreCase = true) -> Triple(
            if (isDark) BadgeVeganBgDark else BadgeVeganBg,
            if (isDark) BadgeVeganTextDark else BadgeVeganText,
            "Vegan / 100% Plant-based"
        )
        badge.equals("G", ignoreCase = true) -> Triple(
            if (isDark) BadgeGlutenFreeBgDark else BadgeGlutenFreeBg,
            if (isDark) BadgeGlutenFreeTextDark else BadgeGlutenFreeText,
            "Gluten-free (Gluteeniton)"
        )
        badge.equals("M", ignoreCase = true) -> Triple(
            if (isDark) BadgeMilkFreeBgDark else BadgeMilkFreeBg,
            if (isDark) BadgeMilkFreeTextDark else BadgeMilkFreeText,
            "Milk-free (Maidoton)"
        )
        badge.equals("Ilmastovalinta", ignoreCase = true) -> Triple(
            if (isDark) BadgeClimateBgDark else BadgeClimateBg,
            if (isDark) BadgeClimateTextDark else BadgeClimateText,
            "Low Carbon Footprint"
        )
        badge.equals("L", ignoreCase = true) -> Triple(
            if (isDark) Color(0xFF064E3B) else Color(0xFFF0FDF4),
            if (isDark) Color(0xFFA7F3D0) else Color(0xFF15803D),
            "Lactose-free (Laktoositon)"
        )
        badge.equals("VL", ignoreCase = true) -> Triple(
            if (isDark) Color(0xFF713F12) else Color(0xFFFEF9C3),
            if (isDark) Color(0xFFFEF08A) else Color(0xFF854D0E),
            "Low lactose"
        )
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, badge)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        val icon = when {
            badge.equals("Veg", ignoreCase = true) -> Icons.Default.Spa
            badge.equals("G", ignoreCase = true) -> Icons.Default.Grass
            badge.equals("M", ignoreCase = true) -> Icons.Default.Opacity
            badge.equals("Ilmastovalinta", ignoreCase = true) -> Icons.Default.Eco
            else -> Icons.Default.Restaurant
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$badge • $desc",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

fun getMealTypeIcon(mealType: MealType): ImageVector {
    return when (mealType) {
        MealType.VEGAN -> Icons.Default.Spa
        MealType.SOUP -> Icons.Default.Opacity
        MealType.PASTA -> Icons.Default.Restaurant
        MealType.FISH -> Icons.Default.Public
        MealType.CHICKEN -> Icons.Default.FitnessCenter
        MealType.MEAT -> Icons.Default.Restaurant
        MealType.BURGER_GRILL -> Icons.Default.LocalFireDepartment
        MealType.SALAD -> Icons.Default.Grass
        MealType.DESSERT -> Icons.Default.LocalFireDepartment
        MealType.CHEF_SPECIAL -> Icons.Default.Restaurant
    }
}

private fun shareMeal(context: Context, meal: Meal, restaurantName: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Today at UniCafe $restaurantName: ${meal.name} (${meal.dietaryBadges.joinToString()}) for ${meal.studentPrice ?: "student lunch"}! Check it on UniCafe Daily."
            )
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share lunch with friends").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.util.Log.e("FoodDetailSheet", "Failed to share meal", e)
    }
}
