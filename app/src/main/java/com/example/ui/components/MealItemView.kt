package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.domain.model.Meal
import com.example.domain.model.MealType
import com.example.ui.theme.BadgeClimateBg
import com.example.ui.theme.BadgeClimateText
import com.example.ui.theme.BadgeGlutenFreeBg
import com.example.ui.theme.BadgeGlutenFreeText
import com.example.ui.theme.BadgeMilkFreeBg
import com.example.ui.theme.BadgeMilkFreeText
import com.example.ui.theme.BadgeNeutralBg
import com.example.ui.theme.BadgeNeutralText
import com.example.ui.theme.BadgeVeganBg
import com.example.ui.theme.BadgeVeganText
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.IconButton
import com.example.ui.theme.BadgeClimateBgDark
import com.example.ui.theme.BadgeClimateTextDark
import com.example.ui.theme.BadgeGlutenFreeBgDark
import com.example.ui.theme.BadgeGlutenFreeTextDark
import com.example.ui.theme.BadgeMilkFreeBgDark
import com.example.ui.theme.BadgeMilkFreeTextDark
import com.example.ui.theme.BadgeVeganBgDark
import com.example.ui.theme.BadgeVeganTextDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealItemView(
    meal: Meal,
    onMealClick: (Meal) -> Unit,
    showDivider: Boolean = true,
    isFavoriteMeal: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    isRecentlyEaten: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        onClick = { onMealClick(meal) },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal_item_${meal.id}")
            .semantics {
                contentDescription = "${meal.category}: ${meal.name}. Student price ${meal.studentPrice ?: "3.10"}"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category Avatar with distinct icon
                MealCategoryAvatar(mealType = meal.mealType)

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    // Category & Price & Favorite Star
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = meal.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            )

                            if (isRecentlyEaten) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Eaten recently",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            meal.studentPrice?.let { price ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = price,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (onToggleFavorite != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable { onToggleFavorite() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFavoriteMeal) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite meal",
                                        tint = if (isFavoriteMeal) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Meal Name
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isFavoriteMeal) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Equal-sized Badges & Quick Highlights Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            meal.dietaryBadges.forEach { badge ->
                                DietaryBadgePill(badge = badge, isDark = isDark)
                            }

                            // Quick calorie highlight if parsed
                            meal.nutritionInfo?.caloriesKcal?.let { kcal ->
                                val calBg = if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
                                val calText = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = calBg,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = calText,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = kcal,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = calText
                                        )
                                    }
                                }
                            }
                        }

                        // Tap Indicator Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "Details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Nutrition and Details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (showDivider) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    thickness = 0.8.dp
                )
            }
        }
    }
}

@Composable
private fun MealCategoryAvatar(mealType: MealType) {
    val (bg, iconColor) = when (mealType) {
        MealType.VEGAN -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
        MealType.SOUP -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        MealType.PASTA -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        MealType.FISH -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        MealType.CHICKEN -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        MealType.MEAT -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        MealType.BURGER_GRILL -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C))
        MealType.SALAD -> Pair(Color(0xFFECFDF5), Color(0xFF047857))
        MealType.DESSERT -> Pair(Color(0xFFFCE7F3), Color(0xFFBE185D))
        MealType.CHEF_SPECIAL -> Pair(Color(0xFFF1F5F9), BrandBlue)
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getMealTypeIcon(mealType),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun DietaryBadgePill(badge: String, isDark: Boolean = false, modifier: Modifier = Modifier) {
    val (bgColor: Color, textColor: Color) = when {
        badge.equals("Veg", ignoreCase = true) -> Pair(
            if (isDark) BadgeVeganBgDark else BadgeVeganBg,
            if (isDark) BadgeVeganTextDark else BadgeVeganText
        )
        badge.equals("G", ignoreCase = true) -> Pair(
            if (isDark) BadgeGlutenFreeBgDark else BadgeGlutenFreeBg,
            if (isDark) BadgeGlutenFreeTextDark else BadgeGlutenFreeText
        )
        badge.equals("M", ignoreCase = true) -> Pair(
            if (isDark) BadgeMilkFreeBgDark else BadgeMilkFreeBg,
            if (isDark) BadgeMilkFreeTextDark else BadgeMilkFreeText
        )
        badge.equals("Ilmastovalinta", ignoreCase = true) -> Pair(
            if (isDark) BadgeClimateBgDark else BadgeClimateBg,
            if (isDark) BadgeClimateTextDark else BadgeClimateText
        )
        else -> Pair(
            if (isDark) MaterialTheme.colorScheme.surfaceVariant else BadgeNeutralBg,
            if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else BadgeNeutralText
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp)
    ) {
        Text(
            text = badge,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
