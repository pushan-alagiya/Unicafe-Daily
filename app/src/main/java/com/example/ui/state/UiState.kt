package com.example.ui.state

import com.example.domain.model.Campus
import com.example.domain.model.DietaryFilter
import com.example.domain.model.EatenMealRecord
import com.example.domain.model.MyDietPreference
import com.example.domain.model.Restaurant

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val favoriteRestaurants: List<Restaurant> = emptyList(),
    val allRestaurants: List<Restaurant> = emptyList(),
    val selectedFilter: DietaryFilter = DietaryFilter.ALL,
    val favoriteIds: Set<Int> = emptySet(),
    val formattedDate: String = "",
    val lastUpdatedText: String? = null,
    val isDataStale: Boolean = false,
    val errorMessage: String? = null,
    val showFavoritePicker: Boolean = false,
    val favoriteMealNames: Set<String> = emptySet(),
    val myDietPreference: MyDietPreference = MyDietPreference.NONE,
    val hideNonMatchingMeals: Boolean = false,
    val selectedCampus: Campus = Campus.ALL,
    val eatenMeals: List<EatenMealRecord> = emptyList(),
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
}

