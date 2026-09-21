package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider as createColorProvider
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.data.repository.UniCafeRepository
import com.example.domain.model.Meal
import com.example.domain.model.Restaurant
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UniCafeGlanceWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_BOX = DpSize(120.dp, 90.dp)
        private val MEDIUM_BOX = DpSize(220.dp, 140.dp)
        private val LARGE_BOX = DpSize(300.dp, 200.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SMALL_BOX, MEDIUM_BOX, LARGE_BOX))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = UniCafeRepository.getInstance(context)
        var cachedRestaurants = repository.getCachedRestaurants() ?: emptyList()
        if (cachedRestaurants.isEmpty()) {
            val result = repository.fetchRestaurants(forceNetwork = false)
            if (result is com.example.data.repository.MenuFetchResult.Success) {
                cachedRestaurants = result.restaurants
            } else if (result is com.example.data.repository.MenuFetchResult.Error && result.cachedRestaurants != null) {
                cachedRestaurants = result.cachedRestaurants
            }
        }
        val orderedFavoriteIds = repository.orderedFavoriteIdsFlow.first()
        val favorites = if (orderedFavoriteIds.isNotEmpty()) {
            orderedFavoriteIds.mapNotNull { favId -> cachedRestaurants.firstOrNull { it.id == favId } }
        } else {
            cachedRestaurants.filter { it.isFavorite }
        }.ifEmpty { cachedRestaurants.take(3) }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ROOT)
        val lastUpdatedStr = timeFormat.format(Date())

        provideContent {
            GlanceTheme {
                WidgetContent(
                    favorites = favorites,
                    lastUpdated = lastUpdatedStr
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        favorites: List<Restaurant>,
        lastUpdated: String
    ) {
        val widgetBg = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
            night = androidx.compose.ui.graphics.Color(0xFF0F172A)
        )
        val headerColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF1E40AF),
            night = androidx.compose.ui.graphics.Color(0xFF60A5FA)
        )
        val primaryText = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF0F172A),
            night = androidx.compose.ui.graphics.Color(0xFFF8FAFC)
        )
        val secondaryText = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF64748B),
            night = androidx.compose.ui.graphics.Color(0xFF94A3B8)
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(widgetBg)
                .cornerRadius(18.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // Widget Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UniCafe Daily",
                    style = TextStyle(
                        color = headerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = "• $lastUpdated",
                    style = TextStyle(
                        color = secondaryText,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (favorites.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap to choose favorite UniCafes",
                        style = TextStyle(color = secondaryText, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(favorites) { restaurant ->
                        RestaurantCardWidget(
                            restaurant = restaurant,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun RestaurantCardWidget(
        restaurant: Restaurant,
        primaryText: ColorProvider,
        secondaryText: ColorProvider
    ) {
        val cardBg = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            night = androidx.compose.ui.graphics.Color(0xFF1E293B)
        )
        val openColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF059669),
            night = androidx.compose.ui.graphics.Color(0xFF34D399)
        )
        val closedColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFFDC2626),
            night = androidx.compose.ui.graphics.Color(0xFFF87171)
        )
        val statusBg = if (restaurant.status.isOpenNow) {
            createColorProvider(
                day = androidx.compose.ui.graphics.Color(0xFFD1FAE5),
                night = androidx.compose.ui.graphics.Color(0xFF064E3B)
            )
        } else {
            createColorProvider(
                day = androidx.compose.ui.graphics.Color(0xFFFEE2E2),
                night = androidx.compose.ui.graphics.Color(0xFF7F1D1D)
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(cardBg)
                .cornerRadius(12.dp)
                .padding(10.dp)
        ) {
            // Header: Name + Campus + Live Status Pill
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = restaurant.name,
                    style = TextStyle(
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = GlanceModifier.width(6.dp))

                Text(
                    text = "• ${restaurant.campus}",
                    style = TextStyle(
                        color = secondaryText,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Status Pill with countdown support
                val countdown = restaurant.status.countdownText
                val statusText = when {
                    restaurant.status.isOpenNow && !countdown.isNullOrBlank() -> "● $countdown"
                    restaurant.status.isOpenNow -> "● OPEN"
                    !countdown.isNullOrBlank() -> "● $countdown"
                    else -> "● CLOSED"
                }

                Box(
                    modifier = GlanceModifier
                        .background(statusBg)
                        .cornerRadius(6.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        style = TextStyle(
                            color = if (restaurant.status.isOpenNow) openColor else closedColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Hours
            Text(
                text = restaurant.status.hoursDescription,
                style = TextStyle(color = secondaryText, fontSize = 11.sp)
            )

            // Meals (Show up to 2 items)
            val previewMeals = restaurant.todaysMeals.take(2)
            if (previewMeals.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                for (meal in previewMeals) {
                    MealRowWidget(meal = meal, primaryText = primaryText, secondaryText = secondaryText)
                }
            }
        }
    }

    @Composable
    private fun MealRowWidget(
        meal: Meal,
        primaryText: ColorProvider,
        secondaryText: ColorProvider
    ) {
        val studentPriceColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF1E40AF),
            night = androidx.compose.ui.graphics.Color(0xFF93C5FD)
        )

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val badgeStr = if (meal.dietaryBadges.isNotEmpty()) {
                "[${meal.dietaryBadges.take(2).joinToString(" ")}] "
            } else ""

            Text(
                text = "• $badgeStr${meal.name}",
                maxLines = 1,
                style = TextStyle(color = primaryText, fontSize = 11.sp),
                modifier = GlanceModifier.defaultWeight()
            )

            meal.studentPrice?.let { price ->
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = price,
                    style = TextStyle(
                        color = studentPriceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
