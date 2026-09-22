package fi.pushan.unicafedaily.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RestaurantDetailSheet(
    restaurant: Restaurant,
    isFavorite: Boolean,
    favoriteMealNames: Set<String> = emptySet(),
    onToggleFavoriteRestaurant: (Int) -> Unit,
    onToggleFavoriteMeal: ((String) -> Unit)? = null,
    onMealClick: (Meal) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("restaurant_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Top Navigation & Action Row (Campus Pill + Status + Favorite, Share, Close)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = restaurant.campus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    StatusBadge(status = restaurant.status)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onToggleFavoriteRestaurant(restaurant.id) },
                        modifier = Modifier.size(36.dp).testTag("toggle_favorite_restaurant_in_sheet")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { shareRestaurantInfo(context, restaurant) },
                        modifier = Modifier.size(36.dp).testTag("share_restaurant_detail")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share cafe",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_restaurant_detail_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restaurant Headline Title
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )

            // Address Row (Clickable to Map)
            if (restaurant.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { openMapIntent(context, restaurant.name, restaurant.address) }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = restaurant.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Redesigned Quick Action Bar: Circular Icon + Label Stack
            // Perfectly fits available space without text clipping or horizontal overflow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (restaurant.address.isNotBlank()) {
                    CafeActionButton(
                        icon = Icons.Default.NearMe,
                        label = "Directions",
                        testTag = "action_map_btn",
                        onClick = { openMapIntent(context, restaurant.name, restaurant.address) }
                    )
                }

                if (!restaurant.phone.isNullOrBlank()) {
                    CafeActionButton(
                        icon = Icons.Default.Call,
                        label = "Call",
                        testTag = "action_call_btn",
                        onClick = { openPhoneIntent(context, restaurant.phone) }
                    )
                }

                if (!restaurant.email.isNullOrBlank()) {
                    CafeActionButton(
                        icon = Icons.Default.Email,
                        label = "Email",
                        testTag = "action_email_btn",
                        onClick = { openEmailIntent(context, restaurant.email) }
                    )
                }

                if (!restaurant.websiteUrl.isNullOrBlank()) {
                    CafeActionButton(
                        icon = Icons.Default.Language,
                        label = "Website",
                        testTag = "action_web_btn",
                        onClick = { openWebIntent(context, restaurant.websiteUrl) }
                    )
                }

                CafeActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    testTag = "action_share_btn",
                    onClick = { shareRestaurantInfo(context, restaurant) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Status & Lunch Hours Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (restaurant.status.isOpenNow) {
                        Color(0xFFDCFCE7)
                    } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (restaurant.status.isOpenNow) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = restaurant.status.hoursDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (restaurant.status.isOpenNow) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        restaurant.status.lunchEndTime?.let { ends ->
                            if (restaurant.status.isOpenNow) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lunch served until $ends",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        restaurant.status.nextOpeningText?.let { nextOpen ->
                            if (!restaurant.status.isOpenNow) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Next opening: $nextOpen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    restaurant.status.countdownText?.let { countText ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (restaurant.status.isOpenNow) Color(0xFF166534) else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = countText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opening Hours Section
            Text(
                text = "OPENING HOURS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val lunchHours = restaurant.visitingHours?.lunchSchedules.orEmpty()
                    if (lunchHours.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lunch Service",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lunchHours.forEach { schedule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = schedule.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = schedule.hours,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (schedule.isClosed) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    val businessHours = restaurant.visitingHours?.businessSchedules.orEmpty()
                    if (businessHours.isNotEmpty()) {
                        if (lunchHours.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cafe & Bistro Hours",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        businessHours.forEach { schedule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = schedule.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = schedule.hours,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (schedule.isClosed) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    if (lunchHours.isEmpty() && businessHours.isEmpty()) {
                        Text(
                            text = restaurant.status.hoursDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Campus Info Note
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "UniCafe ${restaurant.name} serves ${restaurant.campus} campus with accessible entrance and student-subsidized lunch service.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Today's Menu Section (NO individual dish rates displayed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S MENU (${restaurant.todaysMeals.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Student lunch €3.10",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (restaurant.todaysMeals.isEmpty()) {
                Text(
                    text = "No meals listed for today at this restaurant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                restaurant.todaysMeals.forEachIndexed { index, meal ->
                    val isFav = favoriteMealNames.contains(meal.name)
                    MealItemView(
                        meal = meal,
                        onMealClick = onMealClick,
                        showDivider = index < restaurant.todaysMeals.lastIndex,
                        isFavoriteMeal = isFav,
                        onToggleFavorite = if (onToggleFavoriteMeal != null) {
                            { onToggleFavoriteMeal(meal.name) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun CafeActionButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun openMapIntent(context: Context, name: String, address: String) {
    try {
        val query = Uri.encode("UniCafe $name, $address")
        val uri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode("UniCafe $name, $address"))
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun openPhoneIntent(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun openEmailIntent(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
    }
}

private fun openWebIntent(context: Context, url: String) {
    try {
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

private fun shareRestaurantInfo(context: Context, restaurant: Restaurant) {
    try {
        val builder = StringBuilder()
        builder.append("🍽️ UniCafe ${restaurant.name} (${restaurant.campus})\n")
        if (restaurant.address.isNotBlank()) {
            builder.append("📍 ${restaurant.address}\n")
        }
        builder.append("🕒 Lunch: ${restaurant.status.hoursDescription}\n")
        if (!restaurant.websiteUrl.isNullOrBlank()) {
            builder.append("🌐 ${restaurant.websiteUrl}\n")
        }
        builder.append("\nCheck daily menus on UniCafe Daily!")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${restaurant.name}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.util.Log.e("RestaurantDetailSheet", "Failed to share cafe info", e)
    }
}
