package fi.pushan.unicafedaily.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Info
import fi.pushan.unicafedaily.domain.model.CustomerCategory
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    selectedFilter: DietaryFilter,
    favoriteMealNames: Set<String> = emptySet(),
    eatenMealNames: Set<String> = emptySet(),
    dateFormatted: String = "",
    customerCategory: CustomerCategory = CustomerCategory.STUDENT,
    onOpenRestaurantDetail: ((Restaurant) -> Unit)? = null,
    onToggleFavoriteMeal: ((String) -> Unit)? = null,
    onMealClick: (Meal, Restaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Filter meals by selected diet
    val filteredMeals = restaurant.todaysMeals.filter { meal ->
        selectedFilter.matches(meal.dietaryBadges)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("restaurant_card_${restaurant.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Restaurant Name + Status Badge & Share + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onOpenRestaurantDetail != null) {
                                Modifier.clickable { onOpenRestaurantDetail(restaurant) }
                            } else Modifier
                        )
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = restaurant.campus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (restaurant.address.isNotBlank()) {
                            Text(
                                text = restaurant.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = restaurant.status)

                    if (onOpenRestaurantDetail != null) {
                        IconButton(
                            onClick = { onOpenRestaurantDetail(restaurant) },
                            modifier = Modifier.size(32.dp).testTag("info_restaurant_${restaurant.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Restaurant info and opening hours",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { shareRestaurantMenu(context, restaurant, dateFormatted) },
                        modifier = Modifier.size(32.dp).testTag("share_restaurant_${restaurant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lunch Hours Bar with subtle surface background & Next opening / lunch ends
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = restaurant.status.hoursDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        restaurant.status.lunchEndTime?.let { ends ->
                            if (restaurant.status.isOpenNow) {
                                Text(
                                    text = "Lunch ends $ends",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    restaurant.status.nextOpeningText?.let { nextOpen ->
                        if (!restaurant.status.isOpenNow) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Next opening: $nextOpen",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meals List or Empty State
            if (restaurant.todaysMeals.isEmpty()) {
                EmptyMenuState(
                    title = "No menu available for today",
                    subtitle = if (!restaurant.status.isOpenNow)
                        "The restaurant is currently closed or updating its weekly menu."
                    else "Check back shortly for today's lunch options."
                )
            } else if (filteredMeals.isEmpty()) {
                EmptyMenuState(
                    title = "No ${selectedFilter.label} options today",
                    subtitle = "Tap another dietary filter chip to view ${restaurant.todaysMeals.size} other meals."
                )
            } else {
                filteredMeals.forEachIndexed { index, meal ->
                    val isFav = favoriteMealNames.contains(meal.name)
                    val isEaten = eatenMealNames.contains(meal.name)

                    MealItemView(
                        meal = meal,
                        onMealClick = { clickedMeal -> onMealClick(clickedMeal, restaurant) },
                        showDivider = index < filteredMeals.lastIndex,
                        isFavoriteMeal = isFav,
                        onToggleFavorite = if (onToggleFavoriteMeal != null) {
                            { onToggleFavoriteMeal(meal.name) }
                        } else null,
                        isRecentlyEaten = isEaten,
                        customerCategory = customerCategory
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMenuState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

private fun shareRestaurantMenu(context: Context, restaurant: Restaurant, dateFormatted: String) {
    try {
        val builder = StringBuilder()
        builder.append("🍽️ UniCafe ${restaurant.name}\n")
        if (dateFormatted.isNotBlank()) {
            builder.append("📅 $dateFormatted\n")
        }
        builder.append("🕒 Lunch: ${restaurant.status.hoursDescription}\n\n")

        if (restaurant.todaysMeals.isEmpty()) {
            builder.append("No menu available for this date.\n")
        } else {
            restaurant.todaysMeals.forEach { meal ->
                val badges = if (meal.dietaryBadges.isNotEmpty()) " [${meal.dietaryBadges.joinToString()}]" else ""
                val price = meal.studentPrice?.let { " - $it" } ?: ""
                builder.append("• ${meal.name}$badges$price (${meal.category})\n")
            }
        }
        builder.append("\nCheck all menus on UniCafe Daily!")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${restaurant.name} menu").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.util.Log.e("RestaurantCard", "Failed to share menu", e)
    }
}

