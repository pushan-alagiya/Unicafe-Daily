package fi.pushan.unicafedaily.widget

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
import fi.pushan.unicafedaily.MainActivity
import fi.pushan.unicafedaily.data.mapper.RestaurantCanonicalMapper
import fi.pushan.unicafedaily.data.repository.MenuFetchResult
import fi.pushan.unicafedaily.data.repository.UniCafeRepository
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.domain.model.StatusState
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
        val currentLang = repository.appLanguageFlow.first()

        // Fetch or get cache strictly for the current app language
        var restaurants = repository.getCachedRestaurants(currentLang) ?: emptyList()
        if (restaurants.isEmpty()) {
            val result = repository.fetchRestaurants(forceNetwork = true, language = currentLang)
            if (result is MenuFetchResult.Success) {
                restaurants = result.restaurants
            } else if (result is MenuFetchResult.Error && result.cachedRestaurants != null) {
                restaurants = result.cachedRestaurants
            }
        }

        val orderedFavoriteIds = repository.orderedFavoriteIdsFlow.first()
        val favoriteMealNames = repository.favoriteMealNamesFlow.first()
        val customerCategory = repository.customerCategoryFlow.first()

        // Strictly show ONLY favorites matching canonical IDs. Never fall back to all list.
        val favorites = if (orderedFavoriteIds.isNotEmpty()) {
            val canonicalFavIds = orderedFavoriteIds.map { RestaurantCanonicalMapper.getCanonicalId(it) }
            canonicalFavIds.mapNotNull { canonicalId ->
                restaurants.firstOrNull { rest ->
                    RestaurantCanonicalMapper.getCanonicalId(rest.id, rest.slug) == canonicalId
                }?.copy(isFavorite = true)
            }
        } else {
            emptyList()
        }

        val timeLocale = when (currentLang) {
            "fi" -> Locale("fi", "FI")
            "sv" -> Locale("sv", "SE")
            else -> Locale.ENGLISH
        }
        val timeFormat = SimpleDateFormat("HH:mm", timeLocale)
        val lastUpdatedStr = timeFormat.format(Date())
        val strings = getWidgetStrings(currentLang)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    favorites = favorites,
                    favoriteMealNames = favoriteMealNames,
                    customerCategory = customerCategory,
                    lastUpdated = lastUpdatedStr,
                    strings = strings
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        favorites: List<Restaurant>,
        favoriteMealNames: Set<String>,
        customerCategory: fi.pushan.unicafedaily.domain.model.CustomerCategory,
        lastUpdated: String,
        strings: WidgetStrings
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

                val subHeader = if (favorites.isNotEmpty()) {
                    "${strings.favoritesCount(favorites.size)} • $lastUpdated"
                } else {
                    "• $lastUpdated"
                }

                Text(
                    text = subHeader,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.padding(16.dp)
                    ) {
                        Text(
                            text = strings.noFavoritesTitle,
                            style = TextStyle(
                                color = primaryText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = strings.noFavoritesSub,
                            style = TextStyle(
                                color = secondaryText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(favorites) { restaurant ->
                        RestaurantCardWidget(
                            restaurant = restaurant,
                            favoriteMealNames = favoriteMealNames,
                            customerCategory = customerCategory,
                            strings = strings,
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
        favoriteMealNames: Set<String>,
        customerCategory: fi.pushan.unicafedaily.domain.model.CustomerCategory,
        strings: WidgetStrings,
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

                if (restaurant.campus.isNotBlank()) {
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "• ${restaurant.campus}",
                        style = TextStyle(
                            color = secondaryText,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Status Pill with localized text & countdown support
                val countdown = restaurant.status.countdownText
                val statusText = when {
                    restaurant.status.isOpenNow && !countdown.isNullOrBlank() -> {
                        "● " + strings.formatCountdown(countdown)
                    }
                    restaurant.status.isOpenNow -> "● ${strings.openNow}"
                    restaurant.status.state == StatusState.OPENING_SOON && !countdown.isNullOrBlank() -> {
                        "● " + strings.formatCountdown(countdown)
                    }
                    restaurant.status.state == StatusState.OPENING_SOON -> "● ${strings.openingSoon}"
                    countdown?.contains("Closed today", ignoreCase = true) == true -> "● ${strings.closedToday}"
                    else -> "● ${strings.closed}"
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

            // Localized Lunch Hours
            val localizedHours = strings.formatHours(restaurant.status.hoursDescription)
            if (localizedHours.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = localizedHours,
                    style = TextStyle(color = secondaryText, fontSize = 11.sp)
                )
            }

            // Meals (Show all favorites-related meals, prioritizing user's favorite dishes)
            if (restaurant.todaysMeals.isEmpty()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = strings.noMenuToday,
                    style = TextStyle(color = secondaryText, fontSize = 11.sp)
                )
            } else {
                // Prioritize favorite dishes at top
                val (favMeals, regularMeals) = restaurant.todaysMeals.partition { meal ->
                    favoriteMealNames.any { fav -> fav.equals(meal.name, ignoreCase = true) }
                }
                val sortedMeals = favMeals + regularMeals

                Spacer(modifier = GlanceModifier.height(4.dp))
                for (meal in sortedMeals) {
                    val isFavMeal = favoriteMealNames.any { fav -> fav.equals(meal.name, ignoreCase = true) }
                    MealRowWidget(
                        meal = meal,
                        isFavoriteMeal = isFavMeal,
                        customerCategory = customerCategory,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }
            }
        }
    }

    @Composable
    private fun MealRowWidget(
        meal: Meal,
        isFavoriteMeal: Boolean,
        customerCategory: fi.pushan.unicafedaily.domain.model.CustomerCategory,
        primaryText: ColorProvider,
        secondaryText: ColorProvider
    ) {
        val studentPriceColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFF1E40AF),
            night = androidx.compose.ui.graphics.Color(0xFF93C5FD)
        )
        val favStarColor = createColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFFD97706),
            night = androidx.compose.ui.graphics.Color(0xFFFBBF24)
        )

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val favPrefix = if (isFavoriteMeal) "⭐ " else "• "
            val badgeStr = if (meal.dietaryBadges.isNotEmpty()) {
                "[${meal.dietaryBadges.take(2).joinToString(" ")}] "
            } else ""

            Text(
                text = "$favPrefix$badgeStr${meal.name}",
                maxLines = 1,
                style = TextStyle(
                    color = if (isFavoriteMeal) favStarColor else primaryText,
                    fontWeight = if (isFavoriteMeal) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            val displayPrice = meal.priceForCategory(customerCategory) ?: meal.studentPrice
            displayPrice?.let { price ->
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

data class WidgetStrings(
    val favoritesCount: (Int) -> String,
    val noFavoritesTitle: String,
    val noFavoritesSub: String,
    val openNow: String,
    val openingSoon: String,
    val closed: String,
    val closedToday: String,
    val noMenuToday: String,
    val lunchLabel: String,
    val formatCountdown: (String) -> String,
    val formatHours: (String) -> String
)

private fun getWidgetStrings(lang: String): WidgetStrings {
    return when (lang) {
        "fi" -> WidgetStrings(
            favoritesCount = { count -> "Suosikit ($count)" },
            noFavoritesTitle = "Ei valittuja UniCafe-suosikkeja",
            noFavoritesSub = "Valitse suosikit sovelluksesta napauttamalla",
            openNow = "AVOINNA",
            openingSoon = "AUKEAA PIAN",
            closed = "SULJETTU",
            closedToday = "Suljettu tänään",
            noMenuToday = "Ei lounaslistaa tälle päivälle",
            lunchLabel = "Lounas",
            formatCountdown = { cd ->
                cd.replace("left", "jäljellä")
                    .replace("Opens at", "Aukeaa klo")
                    .replace("in", "")
                    .trim()
            },
            formatHours = { hours ->
                hours.replace("Lunch", "Lounas")
                    .replace("Closed today", "Suljettu tänään")
                    .replace("Hours unavailable", "Aukioloajat ei saatavilla")
                    .replace("No lunch service", "Ei lounaspalvelua")
            }
        )
        "sv" -> WidgetStrings(
            favoritesCount = { count -> "Favoriter ($count)" },
            noFavoritesTitle = "Inga UniCafe-favoriter valda",
            noFavoritesSub = "Välj favoriter i appen genom att trycka",
            openNow = "ÖPPET",
            openingSoon = "ÖPPNAR SNART",
            closed = "STÄNGT",
            closedToday = "Stängt idag",
            noMenuToday = "Ingen matsedel för idag",
            lunchLabel = "Lunch",
            formatCountdown = { cd ->
                cd.replace("left", "kvar")
                    .replace("Opens at", "Öppnar kl.")
                    .replace("in", "")
                    .trim()
            },
            formatHours = { hours ->
                hours.replace("Closed today", "Stängt idag")
                    .replace("Hours unavailable", "Öppettider ej tillgängliga")
                    .replace("No lunch service", "Ingen lunchservering")
            }
        )
        else -> WidgetStrings(
            favoritesCount = { count -> "Favorites ($count)" },
            noFavoritesTitle = "No favorite UniCafes selected",
            noFavoritesSub = "Tap to choose favorites in the app",
            openNow = "OPEN",
            openingSoon = "OPENING SOON",
            closed = "CLOSED",
            closedToday = "Closed today",
            noMenuToday = "No menu available for today",
            lunchLabel = "Lunch",
            formatCountdown = { cd -> cd },
            formatHours = { hours -> hours }
        )
    }
}
