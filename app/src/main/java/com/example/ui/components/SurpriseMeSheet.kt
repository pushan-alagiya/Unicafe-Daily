package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DietaryFilter
import com.example.domain.model.Meal
import com.example.domain.model.Restaurant
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SurpriseMeSheet(
    favoriteRestaurants: List<Restaurant>,
    allRestaurants: List<Restaurant>,
    onRecordEaten: (Meal, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentFilter by remember { mutableStateOf(DietaryFilter.ALL) }
    var onlyFavorites by remember { mutableStateOf(favoriteRestaurants.isNotEmpty()) }
    var rollSeed by remember { mutableIntStateOf(0) }
    var hasEatenRecorded by remember { mutableStateOf(false) }

    val poolRestaurants = remember(onlyFavorites, favoriteRestaurants, allRestaurants) {
        if (onlyFavorites && favoriteRestaurants.isNotEmpty()) favoriteRestaurants else allRestaurants
    }

    // Aggregate eligible dishes
    val eligiblePairs = remember(poolRestaurants, currentFilter, rollSeed) {
        val pairs = mutableListOf<Pair<Meal, Restaurant>>()
        for (restaurant in poolRestaurants) {
            for (meal in restaurant.todaysMeals) {
                val matches = when (currentFilter) {
                    DietaryFilter.ALL -> true
                    DietaryFilter.VEG -> meal.dietaryBadges.any { it.equals("Veg", ignoreCase = true) }
                    DietaryFilter.GLUTEN_FREE -> meal.dietaryBadges.any { it.equals("G", ignoreCase = true) }
                    DietaryFilter.MILK_FREE -> meal.dietaryBadges.any { it.equals("M", ignoreCase = true) }
                }
                if (matches) {
                    pairs.add(Pair(meal, restaurant))
                }
            }
        }
        pairs.shuffled()
    }

    val selectedCandidate = eligiblePairs.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("surprise_me_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = BrandCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Surprise Me! 🎲",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_surprise_sheet")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Restaurant pool toggle (Favourites vs All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = {
                        onlyFavorites = true
                        rollSeed++
                        hasEatenRecorded = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (onlyFavorites) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (onlyFavorites) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (onlyFavorites) BrandBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Favourites only",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (onlyFavorites) FontWeight.Bold else FontWeight.Medium,
                            color = if (onlyFavorites) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = {
                        onlyFavorites = false
                        rollSeed++
                        hasEatenRecorded = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (!onlyFavorites) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (!onlyFavorites) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "All UniCafes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!onlyFavorites) FontWeight.Bold else FontWeight.Medium,
                            color = if (!onlyFavorites) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dietary Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    DietaryFilter.ALL to "Any meal",
                    DietaryFilter.VEG to "Vegan",
                    DietaryFilter.GLUTEN_FREE to "Gluten-free",
                    DietaryFilter.MILK_FREE to "Milk-free"
                ).forEach { (filter, label) ->
                    val isSelected = currentFilter == filter
                    Surface(
                        onClick = {
                            currentFilter = filter
                            rollSeed++
                            hasEatenRecorded = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Result Card
            if (selectedCandidate != null) {
                val (meal, restaurant) = selectedCandidate

                AnimatedContent(
                    targetState = selectedCandidate,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "surprise_dish_animation"
                ) { (currentMeal, currentRestaurant) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandBlue.copy(alpha = 0.35f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "📍 ${currentRestaurant.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                StatusBadge(status = currentRestaurant.status)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = currentMeal.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${currentMeal.category} • Student ${currentMeal.studentPrice ?: "€3.10"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (currentMeal.dietaryBadges.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentMeal.dietaryBadges.forEach { badge ->
                                        DietaryBadgePill(badge)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            rollSeed++
                            hasEatenRecorded = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("surprise_try_another_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Try another 🎲", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onRecordEaten(meal, restaurant.name)
                            hasEatenRecorded = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasEatenRecorded) Color(0xFF059669) else BrandBlue
                        ),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("surprise_eat_this_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasEatenRecorded) "Logged! 😋" else "I ate this 😋",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No dishes found matching this filter at ${if (onlyFavorites) "your favourite" else "any"} UniCafes today.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (onlyFavorites) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onlyFavorites = false; rollSeed++ }
                            ) {
                                Text("Check all UniCafes")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
