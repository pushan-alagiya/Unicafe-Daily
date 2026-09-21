package com.example.ui.state

import com.example.domain.model.Campus
import com.example.domain.model.DietaryFilter
import com.example.domain.model.EatenMealRecord
import com.example.domain.model.MyDietPreference
import com.example.domain.model.RecentlyViewedDish
import com.example.domain.model.Restaurant
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
