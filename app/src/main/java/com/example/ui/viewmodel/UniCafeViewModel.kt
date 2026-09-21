package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiConfig
import com.example.data.mapper.UniCafeMapper
import com.example.data.repository.MenuFetchResult
import com.example.data.repository.UniCafeRepository
import com.example.domain.model.DietaryFilter
import com.example.domain.model.Restaurant
import com.example.ui.state.HomeUiState
import com.example.widget.UniCafeWidgetUpdater
import com.example.worker.MenuSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private data class UserPrefsBundle(
    val favIds: Set<Int>,
    val filter: DietaryFilter,
    val lastUpdatedTs: Long?,
    val favMeals: Set<String>,
    val myDiet: com.example.domain.model.MyDietPreference
)

private data class FeaturePrefsBundle(
    val hideNonMatch: Boolean,
    val campus: com.example.domain.model.Campus,
    val eaten: List<com.example.domain.model.EatenMealRecord>,
    val notifs: Boolean
)

class UniCafeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UniCafeRepository.getInstance(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allLoadedRestaurants: List<Restaurant> = emptyList()

    init {
        // Schedule periodic sync via WorkManager
        MenuSyncWorker.schedulePeriodicSync(application)

        // Observe user preferences
        viewModelScope.launch {
            combine(
                repository.favoriteIdsFlow,
                repository.dietaryFilterFlow,
                repository.lastRefreshedFlow,
                repository.favoriteMealNamesFlow,
                repository.myDietPreferenceFlow
            ) { favIds, filter, lastUpdatedTs, favMeals, myDiet ->
                UserPrefsBundle(favIds, filter, lastUpdatedTs, favMeals, myDiet)
            }.collect { bundle ->
                _uiState.update { current ->
                    val updatedFavorites = allLoadedRestaurants
                        .filter { bundle.favIds.contains(it.id) }
                        .map { it.copy(isFavorite = true) }

                    current.copy(
                        favoriteIds = bundle.favIds,
                        selectedFilter = bundle.filter,
                        favoriteRestaurants = updatedFavorites,
                        lastUpdatedText = formatTimestamp(bundle.lastUpdatedTs),
                        formattedDate = getCurrentDateFormatted(),
                        favoriteMealNames = bundle.favMeals,
                        myDietPreference = bundle.myDiet
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.hideNonMatchingMealsFlow,
                repository.campusPreferenceFlow,
                repository.eatenMealsFlow,
                repository.notificationsEnabledFlow
            ) { hideNonMatch, campus, eaten, notifs ->
                FeaturePrefsBundle(hideNonMatch, campus, eaten, notifs)
            }.collect { bundle ->
                _uiState.update { current ->
                    current.copy(
                        hideNonMatchingMeals = bundle.hideNonMatch,
                        selectedCampus = bundle.campus,
                        eatenMeals = bundle.eaten,
                        notificationsEnabled = bundle.notifs
                    )
                }
            }
        }

        loadData(forceNetwork = false)
    }

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.favoriteRestaurants.isEmpty(),
                    isRefreshing = forceNetwork,
                    errorMessage = null,
                    formattedDate = getCurrentDateFormatted()
                )
            }

            when (val result = repository.fetchRestaurants(forceNetwork = forceNetwork)) {
                is MenuFetchResult.Success -> {
                    allLoadedRestaurants = result.restaurants
                    val favIds = _uiState.value.favoriteIds
                    val favorites = allLoadedRestaurants
                        .filter { favIds.contains(it.id) }
                        .map { it.copy(isFavorite = true) }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            allRestaurants = allLoadedRestaurants,
                            favoriteRestaurants = favorites,
                            isDataStale = result.isFromCache,
                            lastUpdatedText = formatTimestamp(result.lastUpdated),
                            errorMessage = null,
                            menuChangeNotice = result.menuChangeNotice
                        )
                    }

                    // Update Glance widget whenever data is successfully updated
                    UniCafeWidgetUpdater.updateAll(getApplication())
                }
                is MenuFetchResult.Error -> {
                    if (result.cachedRestaurants != null) {
                        allLoadedRestaurants = result.cachedRestaurants
                        val favIds = _uiState.value.favoriteIds
                        val favorites = allLoadedRestaurants
                            .filter { favIds.contains(it.id) }
                            .map { it.copy(isFavorite = true) }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                allRestaurants = allLoadedRestaurants,
                                favoriteRestaurants = favorites,
                                isDataStale = true,
                                lastUpdatedText = formatTimestamp(result.lastUpdated),
                                errorMessage = result.message
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onRefreshClicked() {
        loadData(forceNetwork = true)
    }

    fun onDietaryFilterSelected(filter: DietaryFilter) {
        viewModelScope.launch {
            repository.setDietaryFilter(filter)
        }
    }

    fun onToggleFavoriteMeal(mealName: String) {
        viewModelScope.launch {
            repository.toggleFavoriteMeal(mealName)
        }
    }

    fun onSetMyDiet(diet: com.example.domain.model.MyDietPreference) {
        viewModelScope.launch {
            repository.setMyDietPreference(diet)
        }
    }

    fun onToggleHideNonMatching(hide: Boolean) {
        viewModelScope.launch {
            repository.setHideNonMatchingMeals(hide)
        }
    }

    fun onSetCampus(campus: com.example.domain.model.Campus) {
        viewModelScope.launch {
            repository.setCampusPreference(campus)
        }
    }

    fun onRecordEatenMeal(mealName: String, restaurantName: String, priceEur: Double = 3.10, badges: List<String> = emptyList()) {
        viewModelScope.launch {
            repository.recordEatenMeal(mealName, restaurantName, priceEur, badges)
        }
    }

    fun onDeleteEatenMeal(recordId: String) {
        viewModelScope.launch {
            repository.deleteEatenMeal(recordId)
        }
    }

    fun onOpenLeavingForLunch() {
        _uiState.update { it.copy(showLeavingForLunchSheet = true) }
    }

    fun onDismissLeavingForLunch() {
        _uiState.update { it.copy(showLeavingForLunchSheet = false) }
    }

    fun onOpenSurpriseMe() {
        _uiState.update { it.copy(showSurpriseMeSheet = true) }
    }

    fun onDismissSurpriseMe() {
        _uiState.update { it.copy(showSurpriseMeSheet = false) }
    }

    fun onOpenBudgetHistory() {
        _uiState.update { it.copy(showBudgetHistorySheet = true) }
    }

    fun onDismissBudgetHistory() {
        _uiState.update { it.copy(showBudgetHistorySheet = false) }
    }

    fun onDismissMenuNotice() {
        _uiState.update { it.copy(menuChangeNotice = null) }
    }

    fun onToggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun onOpenFavoritePicker() {
        _uiState.update { it.copy(showFavoritePicker = true) }
    }

    fun onDismissFavoritePicker() {
        _uiState.update { it.copy(showFavoritePicker = false) }
    }

    fun onSaveFavorites(newFavoriteIds: Set<Int>) {
        viewModelScope.launch {
            val sanitized = newFavoriteIds.take(ApiConfig.MAX_FAVORITES).toSet()
            repository.setFavorites(sanitized)
            _uiState.update { it.copy(showFavoritePicker = false) }
            UniCafeWidgetUpdater.updateAll(getApplication())
        }
    }

    private fun getCurrentDateFormatted(): String {
        val today = LocalDate.now(UniCafeMapper.HELSINKI_ZONE)
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
        return today.format(formatter)
    }

    private fun formatTimestamp(timestamp: Long?): String? {
        if (timestamp == null || timestamp <= 0) return null
        val sdf = SimpleDateFormat("HH:mm", Locale.ROOT)
        return sdf.format(Date(timestamp))
    }
}
