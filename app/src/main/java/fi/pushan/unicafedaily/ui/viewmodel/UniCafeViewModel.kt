package fi.pushan.unicafedaily.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.pushan.unicafedaily.data.api.ApiConfig
import fi.pushan.unicafedaily.data.mapper.RestaurantCanonicalMapper
import fi.pushan.unicafedaily.data.mapper.UniCafeMapper
import fi.pushan.unicafedaily.data.repository.MenuFetchResult
import fi.pushan.unicafedaily.data.repository.UniCafeRepository
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.ui.state.AppTab
import fi.pushan.unicafedaily.ui.state.HomeUiState
import fi.pushan.unicafedaily.widget.UniCafeWidgetUpdater
import fi.pushan.unicafedaily.worker.MenuSyncWorker
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

private data class PrefsPart1(
    val favIds: Set<Int>,
    val orderedFavIds: List<Int>,
    val filter: DietaryFilter,
    val lastUpdatedTs: Long?
)

private data class PrefsPart2(
    val favMeals: Set<String>,
    val myDiet: fi.pushan.unicafedaily.domain.model.MyDietPreference,
    val lang: String,
    val statusFilter: String
)

private data class UserPrefsBundle(
    val favIds: Set<Int>,
    val orderedFavIds: List<Int>,
    val filter: DietaryFilter,
    val lastUpdatedTs: Long?,
    val favMeals: Set<String>,
    val myDiet: fi.pushan.unicafedaily.domain.model.MyDietPreference,
    val appLang: String,
    val statusFilter: String
)

private data class FeaturePrefsBundle(
    val hideNonMatch: Boolean,
    val campus: fi.pushan.unicafedaily.domain.model.Campus,
    val eaten: List<fi.pushan.unicafedaily.domain.model.EatenMealRecord>,
    val notifs: Boolean,
    val budgetEur: Double?,
    val recentlyViewed: List<fi.pushan.unicafedaily.domain.model.RecentlyViewedDish>
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
            val part1Flow = combine(
                repository.favoriteIdsFlow,
                repository.orderedFavoriteIdsFlow,
                repository.dietaryFilterFlow,
                repository.lastRefreshedFlow
            ) { favIds, orderedFavIds, filter, lastUpdatedTs ->
                PrefsPart1(favIds, orderedFavIds, filter, lastUpdatedTs)
            }

            val part2Flow = combine(
                repository.favoriteMealNamesFlow,
                repository.myDietPreferenceFlow,
                repository.appLanguageFlow,
                repository.statusFilterFlow
            ) { favMeals, myDiet, lang, statusFilter ->
                PrefsPart2(favMeals, myDiet, lang, statusFilter)
            }

            combine(part1Flow, part2Flow) { p1, p2 ->
                UserPrefsBundle(
                    favIds = p1.favIds,
                    orderedFavIds = p1.orderedFavIds,
                    filter = p1.filter,
                    lastUpdatedTs = p1.lastUpdatedTs,
                    favMeals = p2.favMeals,
                    myDiet = p2.myDiet,
                    appLang = p2.lang,
                    statusFilter = p2.statusFilter
                )
            }.collect { bundle ->
                _uiState.update { current ->
                    // Order favorites according to user's saved ordered list
                    val orderedFavorites = bundle.orderedFavIds.mapNotNull { id ->
                        val canonicalFavId = RestaurantCanonicalMapper.getCanonicalId(id)
                        allLoadedRestaurants.firstOrNull { rest ->
                            RestaurantCanonicalMapper.getCanonicalId(rest.id, rest.slug) == canonicalFavId
                        }?.copy(isFavorite = true)
                    }

                    current.copy(
                        favoriteIds = bundle.favIds,
                        orderedFavoriteIds = bundle.orderedFavIds,
                        selectedFilter = bundle.filter,
                        favoriteRestaurants = orderedFavorites,
                        lastUpdatedText = formatTimestamp(bundle.lastUpdatedTs),
                        formattedDate = getCurrentDateFormatted(bundle.appLang),
                        favoriteMealNames = bundle.favMeals,
                        myDietPreference = bundle.myDiet,
                        appLanguage = bundle.appLang,
                        statusFilter = bundle.statusFilter
                    )
                }
                UniCafeWidgetUpdater.updateAll(getApplication())
            }
        }

        viewModelScope.launch {
            val f1 = combine(
                repository.hideNonMatchingMealsFlow,
                repository.campusPreferenceFlow,
                repository.eatenMealsFlow,
                repository.notificationsEnabledFlow
            ) { hide, camp, eaten, notifs ->
                Tuple4(hide, camp, eaten, notifs)
            }

            combine(
                f1,
                repository.monthlyBudgetEurFlow,
                repository.recentlyViewedFlow
            ) { t, budget, recent ->
                FeaturePrefsBundle(
                    hideNonMatch = t.a,
                    campus = t.b,
                    eaten = t.c,
                    notifs = t.d,
                    budgetEur = budget,
                    recentlyViewed = recent
                )
            }.collect { bundle ->
                _uiState.update { current ->
                    current.copy(
                        hideNonMatchingMeals = bundle.hideNonMatch,
                        selectedCampus = bundle.campus,
                        eatenMeals = bundle.eaten,
                        notificationsEnabled = bundle.notifs,
                        monthlyBudgetEur = bundle.budgetEur,
                        recentlyViewedDishes = bundle.recentlyViewed
                    )
                }
            }
        }

        loadData(forceNetwork = false)
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.favoriteRestaurants.isEmpty() && it.allRestaurants.isEmpty(),
                    isRefreshing = forceNetwork,
                    errorMessage = null,
                    formattedDate = getCurrentDateFormatted(it.appLanguage)
                )
            }

            when (val result = repository.fetchRestaurants(forceNetwork = forceNetwork)) {
                is MenuFetchResult.Success -> {
                    allLoadedRestaurants = result.restaurants
                    val orderedFavIds = _uiState.value.orderedFavoriteIds
                    val favorites = orderedFavIds.mapNotNull { id ->
                        val canonicalFavId = RestaurantCanonicalMapper.getCanonicalId(id)
                        allLoadedRestaurants.firstOrNull { rest ->
                            RestaurantCanonicalMapper.getCanonicalId(rest.id, rest.slug) == canonicalFavId
                        }?.copy(isFavorite = true)
                    }

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

                    // Also preload/refresh future date menu if active
                    loadMenuForDate(_uiState.value.selectedMenuDate)

                    // Update Glance widget
                    UniCafeWidgetUpdater.updateAll(getApplication())
                }
                is MenuFetchResult.Error -> {
                    if (result.cachedRestaurants != null) {
                        allLoadedRestaurants = result.cachedRestaurants
                        val orderedFavIds = _uiState.value.orderedFavoriteIds
                        val favorites = orderedFavIds.mapNotNull { id ->
                            val canonicalFavId = RestaurantCanonicalMapper.getCanonicalId(id)
                            allLoadedRestaurants.firstOrNull { rest ->
                                RestaurantCanonicalMapper.getCanonicalId(rest.id, rest.slug) == canonicalFavId
                            }?.copy(isFavorite = true)
                        }

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
                        loadMenuForDate(_uiState.value.selectedMenuDate)
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

    fun onTabSelected(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == AppTab.MENUS && _uiState.value.futureDateRestaurants.isEmpty()) {
            loadMenuForDate(_uiState.value.selectedMenuDate)
        }
    }

    fun onMenuDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedMenuDate = date) }
        loadMenuForDate(date)
    }

    private fun loadMenuForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFutureDateLoading = true) }
            val restaurantsForDate = repository.fetchRestaurantsForDate(date)
            _uiState.update {
                it.copy(
                    futureDateRestaurants = restaurantsForDate,
                    isFutureDateLoading = false
                )
            }
        }
    }

    fun onDietaryFilterSelected(filter: DietaryFilter) {
        viewModelScope.launch {
            repository.setDietaryFilter(filter)
        }
    }

    fun onStatusFilterSelected(status: String) {
        viewModelScope.launch {
            repository.setStatusFilter(status)
        }
    }

    fun onToggleFavoriteMeal(mealName: String) {
        viewModelScope.launch {
            repository.toggleFavoriteMeal(mealName)
        }
    }

    fun onSetAppLanguage(lang: String) {
        viewModelScope.launch {
            repository.setAppLanguage(lang)
            _uiState.update { it.copy(appLanguage = lang, formattedDate = getCurrentDateFormatted(lang)) }
            loadData(forceNetwork = true)
        }
    }

    fun onSetMyDiet(diet: fi.pushan.unicafedaily.domain.model.MyDietPreference) {
        viewModelScope.launch {
            repository.setMyDietPreference(diet)
        }
    }

    fun onToggleHideNonMatching(hide: Boolean) {
        viewModelScope.launch {
            repository.setHideNonMatchingMeals(hide)
        }
    }

    fun onSetCampus(campus: fi.pushan.unicafedaily.domain.model.Campus) {
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

    fun onSaveMonthlyBudget(budget: Double?) {
        viewModelScope.launch {
            repository.saveMonthlyBudget(budget)
        }
    }

    fun onViewMeal(meal: fi.pushan.unicafedaily.domain.model.Meal, restaurantName: String) {
        viewModelScope.launch {
            repository.recordRecentlyViewed(
                fi.pushan.unicafedaily.domain.model.RecentlyViewedDish(
                    mealName = meal.name,
                    restaurantName = restaurantName,
                    category = meal.category,
                    studentPrice = meal.studentPrice,
                    dietaryBadges = meal.dietaryBadges
                )
            )
        }
    }

    fun onClearRecentlyViewed() {
        viewModelScope.launch {
            repository.clearRecentlyViewed()
        }
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

    fun onOpenFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }

    fun onDismissFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun onOpenDietaryLegend() {
        _uiState.update { it.copy(showDietaryLegendSheet = true) }
    }

    fun onDismissDietaryLegend() {
        _uiState.update { it.copy(showDietaryLegendSheet = false) }
    }

    fun onSaveFavorites(newFavoriteIds: Set<Int>) {
        viewModelScope.launch {
            val sanitized = newFavoriteIds.map { RestaurantCanonicalMapper.getCanonicalId(it) }.distinct().take(ApiConfig.MAX_FAVORITES)
            repository.setOrderedFavorites(sanitized)
            _uiState.update { it.copy(showFavoritePicker = false) }
            UniCafeWidgetUpdater.updateAll(getApplication())
        }
    }

    fun onReorderFavorites(newOrderedList: List<Int>) {
        viewModelScope.launch {
            val sanitized = newOrderedList.map { RestaurantCanonicalMapper.getCanonicalId(it) }.distinct().take(ApiConfig.MAX_FAVORITES)
            repository.setOrderedFavorites(sanitized)
            UniCafeWidgetUpdater.updateAll(getApplication())
        }
    }

    fun onAddFavorite(restaurantId: Int) {
        viewModelScope.launch {
            val canonicalId = RestaurantCanonicalMapper.getCanonicalId(restaurantId)
            val current = _uiState.value.orderedFavoriteIds.map { RestaurantCanonicalMapper.getCanonicalId(it) }.toMutableList()
            if (!current.contains(canonicalId) && current.size < ApiConfig.MAX_FAVORITES) {
                current.add(canonicalId)
                repository.setOrderedFavorites(current)
                UniCafeWidgetUpdater.updateAll(getApplication())
            }
        }
    }

    fun onRemoveFavorite(restaurantId: Int) {
        viewModelScope.launch {
            val canonicalId = RestaurantCanonicalMapper.getCanonicalId(restaurantId)
            val current = _uiState.value.orderedFavoriteIds.map { RestaurantCanonicalMapper.getCanonicalId(it) }.toMutableList()
            current.remove(canonicalId)
            repository.setOrderedFavorites(current)
            UniCafeWidgetUpdater.updateAll(getApplication())
        }
    }

    private fun getCurrentDateFormatted(language: String = "en"): String {
        val today = LocalDate.now(UniCafeMapper.HELSINKI_ZONE)
        val locale = when (language) {
            "fi" -> Locale("fi", "FI")
            "sv" -> Locale("sv", "SE")
            else -> Locale.ENGLISH
        }
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
        return today.format(formatter)
    }

    private fun formatTimestamp(timestamp: Long?): String? {
        if (timestamp == null || timestamp <= 0) return null
        val sdf = SimpleDateFormat("HH:mm", Locale.ROOT)
        return sdf.format(Date(timestamp))
    }
}
