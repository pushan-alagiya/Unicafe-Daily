package fi.pushan.unicafedaily.ui.state

import fi.pushan.unicafedaily.domain.model.Campus
import fi.pushan.unicafedaily.domain.model.CustomerCategory
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.EatenMealRecord
import fi.pushan.unicafedaily.domain.model.MyDietPreference
import fi.pushan.unicafedaily.domain.model.RecentlyViewedDish
import fi.pushan.unicafedaily.domain.model.Restaurant
import java.time.LocalDate

enum class AppTab {
    TODAY,
    MENUS,
    SETTINGS
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedTab: AppTab = AppTab.TODAY,
    val favoriteRestaurants: List<Restaurant> = emptyList(),
    val allRestaurants: List<Restaurant> = emptyList(),
    val selectedFilter: DietaryFilter = DietaryFilter.ALL,
    val favoriteIds: Set<Int> = emptySet(),
    val orderedFavoriteIds: List<Int> = emptyList(),
    val formattedDate: String = "",
    val lastUpdatedText: String? = null,
    val isDataStale: Boolean = false,
    val errorMessage: String? = null,
    val showFavoritePicker: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showDietaryLegendSheet: Boolean = false,
    val showFavoriteDishesSheet: Boolean = false,
    val showUniCafeInfoSheet: Boolean = false,
    val selectedRestaurantForDetail: Restaurant? = null,
    val customerCategory: CustomerCategory = CustomerCategory.STUDENT,
    val statusFilter: String = "ALL",
    val appLanguage: String = "en",
    val selectedMenuDate: LocalDate = LocalDate.now(),
    val futureDateRestaurants: List<Restaurant> = emptyList(),
    val isFutureDateLoading: Boolean = false,
    val favoriteMealNames: Set<String> = emptySet(),
    val myDietPreference: MyDietPreference = MyDietPreference.NONE,
    val hideNonMatchingMeals: Boolean = false,
    val selectedCampus: Campus = Campus.ALL,
    val eatenMeals: List<EatenMealRecord> = emptyList(),
    val recentlyViewedDishes: List<RecentlyViewedDish> = emptyList(),
    val monthlyBudgetEur: Double? = null,
    val menuChangeNotice: String? = null,
    val showLeavingForLunchSheet: Boolean = false,
    val showSurpriseMeSheet: Boolean = false,
    val showBudgetHistorySheet: Boolean = false,
    val notificationsEnabled: Boolean = false
) {
    val monthlySpentEur: Double
        get() = eatenMeals.sumOf { it.priceEur }

    val monthlyMealCount: Int
        get() = eatenMeals.size

    val averageMealPriceEur: Double
        get() = if (monthlyMealCount > 0) monthlySpentEur / monthlyMealCount else 0.0
}
