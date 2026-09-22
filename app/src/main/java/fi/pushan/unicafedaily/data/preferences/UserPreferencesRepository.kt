package fi.pushan.unicafedaily.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fi.pushan.unicafedaily.data.api.ApiConfig
import fi.pushan.unicafedaily.domain.model.Campus
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.EatenMealRecord
import fi.pushan.unicafedaily.domain.model.MyDietPreference
import fi.pushan.unicafedaily.domain.model.RecentlyViewedDish
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unicafe_user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_FAVORITES = stringSetPreferencesKey("favorite_restaurant_ids")
        private val KEY_ORDERED_FAVORITES = stringPreferencesKey("ordered_favorite_restaurant_ids")
        private val KEY_DIETARY_FILTER = stringPreferencesKey("selected_dietary_filter")
        private val KEY_STATUS_FILTER = stringPreferencesKey("restaurant_status_filter")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_LAST_REFRESHED = longPreferencesKey("last_refreshed_timestamp")
        private val KEY_CACHED_JSON = stringPreferencesKey("cached_restaurants_json")
        private val KEY_CACHED_JSON_LANG = stringPreferencesKey("cached_restaurants_json_lang")
        private val KEY_FAVORITE_MEALS = stringSetPreferencesKey("favorite_meal_names")
        private val KEY_MY_DIET = stringPreferencesKey("my_diet_preference")
        private val KEY_HIDE_NON_MATCHING = booleanPreferencesKey("hide_non_matching_meals")
        private val KEY_CAMPUS = stringPreferencesKey("campus_preference")
        private val KEY_EATEN_MEALS_JSON = stringPreferencesKey("eaten_meals_json")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_MONTHLY_BUDGET_EUR = stringPreferencesKey("monthly_budget_eur")
        private val KEY_RECENTLY_VIEWED_JSON = stringPreferencesKey("recently_viewed_dishes_json")
        private val KEY_CUSTOMER_CATEGORY = stringPreferencesKey("customer_category")

        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val eatenMealsListType = Types.newParameterizedType(List::class.java, EatenMealRecord::class.java)
    private val eatenMealsAdapter = moshi.adapter<List<EatenMealRecord>>(eatenMealsListType)
    private val recentlyViewedListType = Types.newParameterizedType(List::class.java, RecentlyViewedDish::class.java)
    private val recentlyViewedAdapter = moshi.adapter<List<RecentlyViewedDish>>(recentlyViewedListType)

    val orderedFavoriteRestaurantIdsFlow: Flow<List<Int>> = context.dataStore.data
        .map { prefs ->
            val orderedStr = prefs[KEY_ORDERED_FAVORITES]
            if (!orderedStr.isNullOrBlank()) {
                orderedStr.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .distinct()
                    .take(ApiConfig.MAX_FAVORITES)
            } else {
                val set = prefs[KEY_FAVORITES]
                if (set.isNullOrEmpty()) {
                    ApiConfig.DEFAULT_FAVORITE_IDS
                } else {
                    set.mapNotNull { it.toIntOrNull() }.take(ApiConfig.MAX_FAVORITES)
                }
            }
        }
        .distinctUntilChanged()

    val favoriteRestaurantIdsFlow: Flow<Set<Int>> = orderedFavoriteRestaurantIdsFlow
        .map { it.toSet() }
        .distinctUntilChanged()

    val appLanguageFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_APP_LANGUAGE] ?: "en" }
        .distinctUntilChanged()

    val statusFilterFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_STATUS_FILTER] ?: "ALL" }
        .distinctUntilChanged()

    val dietaryFilterFlow: Flow<DietaryFilter> = context.dataStore.data
        .map { prefs ->
            val filterName = prefs[KEY_DIETARY_FILTER] ?: DietaryFilter.ALL.name
            try {
                DietaryFilter.valueOf(filterName)
            } catch (_: Exception) {
                DietaryFilter.ALL
            }
        }
        .distinctUntilChanged()

    val lastRefreshedTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[KEY_LAST_REFRESHED] }
        .distinctUntilChanged()

    val cachedJsonFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_CACHED_JSON] }
        .distinctUntilChanged()

    val cachedJsonLangFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_CACHED_JSON_LANG] }
        .distinctUntilChanged()

    val favoriteMealNamesFlow: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[KEY_FAVORITE_MEALS] ?: emptySet() }
        .distinctUntilChanged()

    val myDietPreferenceFlow: Flow<MyDietPreference> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[KEY_MY_DIET] ?: MyDietPreference.NONE.name
            try {
                MyDietPreference.valueOf(raw)
            } catch (_: Exception) {
                MyDietPreference.NONE
            }
        }
        .distinctUntilChanged()

    val customerCategoryFlow: Flow<fi.pushan.unicafedaily.domain.model.CustomerCategory> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[KEY_CUSTOMER_CATEGORY]
            fi.pushan.unicafedaily.domain.model.CustomerCategory.fromId(raw)
        }
        .distinctUntilChanged()

    val hideNonMatchingMealsFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_HIDE_NON_MATCHING] ?: false }
        .distinctUntilChanged()

    val campusPreferenceFlow: Flow<Campus> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[KEY_CAMPUS] ?: Campus.ALL.name
            try {
                Campus.valueOf(raw)
            } catch (_: Exception) {
                Campus.ALL
            }
        }
        .distinctUntilChanged()

    val eatenMealsFlow: Flow<List<EatenMealRecord>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[KEY_EATEN_MEALS_JSON]
            if (json.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    eatenMealsAdapter.fromJson(json) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
        .distinctUntilChanged()

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] ?: false }
        .distinctUntilChanged()

    val monthlyBudgetEurFlow: Flow<Double?> = context.dataStore.data
        .map { prefs -> prefs[KEY_MONTHLY_BUDGET_EUR]?.toDoubleOrNull() }
        .distinctUntilChanged()

    val recentlyViewedFlow: Flow<List<RecentlyViewedDish>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[KEY_RECENTLY_VIEWED_JSON]
            if (json.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    recentlyViewedAdapter.fromJson(json) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
        .distinctUntilChanged()

    suspend fun saveFavorites(favoriteIds: Set<Int>) {
        val limited = favoriteIds.take(ApiConfig.MAX_FAVORITES)
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITES] = limited.map { it.toString() }.toSet()
            prefs[KEY_ORDERED_FAVORITES] = limited.joinToString(",")
        }
    }

    suspend fun saveOrderedFavorites(orderedIds: List<Int>) {
        val limited = orderedIds.distinct().take(ApiConfig.MAX_FAVORITES)
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITES] = limited.map { it.toString() }.toSet()
            prefs[KEY_ORDERED_FAVORITES] = limited.joinToString(",")
        }
    }

    suspend fun saveAppLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = languageCode
        }
    }

    suspend fun saveStatusFilter(filter: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STATUS_FILTER] = filter
        }
    }

    suspend fun saveDietaryFilter(filter: DietaryFilter) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DIETARY_FILTER] = filter.name
        }
    }

    suspend fun saveCachedJson(json: String, lang: String = "en", timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CACHED_JSON] = json
            prefs[KEY_CACHED_JSON_LANG] = lang
            prefs[KEY_LAST_REFRESHED] = timestamp
        }
    }

    suspend fun toggleFavoriteMeal(mealName: String) {
        val normalized = mealName.trim()
        if (normalized.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_MEALS]?.toMutableSet() ?: mutableSetOf()
            val existing = current.firstOrNull { it.equals(normalized, ignoreCase = true) }
            if (existing != null) {
                current.remove(existing)
            } else {
                current.add(normalized)
            }
            prefs[KEY_FAVORITE_MEALS] = current
        }
    }

    suspend fun setMyDietPreference(preference: MyDietPreference) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MY_DIET] = preference.name
        }
    }

    suspend fun setHideNonMatchingMeals(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIDE_NON_MATCHING] = hide
        }
    }

    suspend fun setCampusPreference(campus: Campus) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CAMPUS] = campus.name
        }
    }

    suspend fun recordEatenMeal(mealName: String, restaurantName: String, priceEur: Double = 3.10, badges: List<String> = emptyList()) {
        val newRecord = EatenMealRecord(
            mealName = mealName,
            restaurantName = restaurantName,
            priceEur = priceEur,
            dietaryBadges = badges
        )
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_EATEN_MEALS_JSON]
            val currentList = if (!json.isNullOrBlank()) {
                try { eatenMealsAdapter.fromJson(json)?.toMutableList() ?: mutableListOf() } catch (_: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
            currentList.add(0, newRecord) // prepend to front
            prefs[KEY_EATEN_MEALS_JSON] = eatenMealsAdapter.toJson(currentList.take(100))
        }
    }

    suspend fun deleteEatenMeal(recordId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_EATEN_MEALS_JSON] ?: return@edit
            val currentList = try { eatenMealsAdapter.fromJson(json)?.toMutableList() ?: return@edit } catch (_: Exception) { return@edit }
            currentList.removeAll { it.id == recordId }
            prefs[KEY_EATEN_MEALS_JSON] = eatenMealsAdapter.toJson(currentList)
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveMonthlyBudget(budget: Double?) {
        context.dataStore.edit { prefs ->
            if (budget != null && budget > 0) {
                prefs[KEY_MONTHLY_BUDGET_EUR] = budget.toString()
            } else {
                prefs.remove(KEY_MONTHLY_BUDGET_EUR)
            }
        }
    }

    suspend fun recordRecentlyViewed(dish: RecentlyViewedDish) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_RECENTLY_VIEWED_JSON]
            val currentList = if (!json.isNullOrBlank()) {
                try { recentlyViewedAdapter.fromJson(json)?.toMutableList() ?: mutableListOf() } catch (_: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
            // Remove existing if matching meal name and restaurant
            currentList.removeAll { it.mealName.equals(dish.mealName, ignoreCase = true) && it.restaurantName.equals(dish.restaurantName, ignoreCase = true) }
            currentList.add(0, dish)
            prefs[KEY_RECENTLY_VIEWED_JSON] = recentlyViewedAdapter.toJson(currentList.take(20))
        }
    }

    suspend fun clearRecentlyViewed() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_RECENTLY_VIEWED_JSON)
        }
    }

    suspend fun setCustomerCategory(category: fi.pushan.unicafedaily.domain.model.CustomerCategory) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOMER_CATEGORY] = category.id
        }
    }
}
