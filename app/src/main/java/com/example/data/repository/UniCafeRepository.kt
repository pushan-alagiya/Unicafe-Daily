package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.UniCafeApiClient
import com.example.data.dto.RestaurantDto
import com.example.data.mapper.UniCafeMapper
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.DietaryFilter
import com.example.domain.model.Restaurant
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

sealed class MenuFetchResult {
    data class Success(
        val restaurants: List<Restaurant>,
        val isFromCache: Boolean = false,
        val lastUpdated: Long? = null,
        val menuChangeNotice: String? = null
    ) : MenuFetchResult()
    data class Error(val message: String, val cachedRestaurants: List<Restaurant>? = null, val lastUpdated: Long? = null) : MenuFetchResult()
}

class UniCafeRepository(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(context)
) {
    companion object {
        private const val TAG = "UniCafeRepository"

        @Volatile
        private var INSTANCE: UniCafeRepository? = null

        fun getInstance(context: Context): UniCafeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UniCafeRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val restaurantListType = Types.newParameterizedType(List::class.java, RestaurantDto::class.java)
    private val jsonAdapter = UniCafeApiClient.moshi.adapter<List<RestaurantDto>>(restaurantListType)

    val favoriteIdsFlow: Flow<Set<Int>> = preferencesRepository.favoriteRestaurantIdsFlow
    val dietaryFilterFlow: Flow<DietaryFilter> = preferencesRepository.dietaryFilterFlow
    val lastRefreshedFlow: Flow<Long?> = preferencesRepository.lastRefreshedTimestampFlow
    val favoriteMealNamesFlow: Flow<Set<String>> = preferencesRepository.favoriteMealNamesFlow
    val myDietPreferenceFlow = preferencesRepository.myDietPreferenceFlow
    val hideNonMatchingMealsFlow = preferencesRepository.hideNonMatchingMealsFlow
    val campusPreferenceFlow = preferencesRepository.campusPreferenceFlow
    val eatenMealsFlow = preferencesRepository.eatenMealsFlow
    val notificationsEnabledFlow = preferencesRepository.notificationsEnabledFlow

    suspend fun fetchRestaurants(forceNetwork: Boolean = false): MenuFetchResult = withContext(Dispatchers.IO) {
        val favoriteIds = preferencesRepository.favoriteRestaurantIdsFlow.first()
        val cachedJson = preferencesRepository.cachedJsonFlow.first()
        val lastUpdated = preferencesRepository.lastRefreshedTimestampFlow.first()

        try {
            // Attempt network fetch
            val responseDtos = UniCafeApiClient.api.getRestaurants()
            val now = System.currentTimeMillis()

            val domainModels = responseDtos.map { dto ->
                UniCafeMapper.mapToRestaurant(dto, favoriteIds)
            }

            // Menu change detection
            var changeNotice: String? = null
            if (!cachedJson.isNullOrBlank()) {
                try {
                    val prevDtos = jsonAdapter.fromJson(cachedJson) ?: emptyList()
                    val prevFavModels = prevDtos.filter { favoriteIds.contains(it.id) }
                        .map { UniCafeMapper.mapToRestaurant(it, favoriteIds) }
                    val newFavModels = domainModels.filter { favoriteIds.contains(it.id) }

                    for (newR in newFavModels) {
                        val prevR = prevFavModels.firstOrNull { it.id == newR.id }
                        if (prevR != null) {
                            val prevMealNames = prevR.todaysMeals.map { it.name.trim().lowercase() }.toSet()
                            val newAdded = newR.todaysMeals.firstOrNull { !prevMealNames.contains(it.name.trim().lowercase()) }
                            if (newAdded != null && prevMealNames.isNotEmpty()) {
                                changeNotice = "Menu updated: ${newR.name} added '${newAdded.name}'"
                                break
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            val jsonString = jsonAdapter.toJson(responseDtos)
            preferencesRepository.saveCachedJson(jsonString, now)

            MenuFetchResult.Success(
                restaurants = domainModels,
                isFromCache = false,
                lastUpdated = now,
                menuChangeNotice = changeNotice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed: ${e.message}", e)

            // Try fallback to local cache
            if (!cachedJson.isNullOrBlank()) {
                try {
                    val cachedDtos = jsonAdapter.fromJson(cachedJson)
                    if (!cachedDtos.isNullOrEmpty()) {
                        val cachedDomainModels = cachedDtos.map { dto ->
                            UniCafeMapper.mapToRestaurant(
                                dto = dto,
                                favoriteIds = favoriteIds,
                                targetDate = LocalDate.now(UniCafeMapper.HELSINKI_ZONE),
                                targetTime = LocalTime.now(UniCafeMapper.HELSINKI_ZONE)
                            )
                        }
                        return@withContext MenuFetchResult.Error(
                            message = "Couldn't refresh today's menu. Showing cached data.",
                            cachedRestaurants = cachedDomainModels,
                            lastUpdated = lastUpdated
                        )
                    }
                } catch (parseEx: Exception) {
                    Log.e(TAG, "Failed to parse cached JSON: ${parseEx.message}", parseEx)
                }
            }

            MenuFetchResult.Error(
                message = "Unable to load today's menu. Check your internet connection and try again.",
                cachedRestaurants = null,
                lastUpdated = null
            )
        }
    }

    suspend fun getCachedRestaurants(): List<Restaurant>? = withContext(Dispatchers.IO) {
        val cachedJson = preferencesRepository.cachedJsonFlow.first() ?: return@withContext null
        val favoriteIds = preferencesRepository.favoriteRestaurantIdsFlow.first()
        try {
            val dtos = jsonAdapter.fromJson(cachedJson) ?: return@withContext null
            dtos.map { dto ->
                UniCafeMapper.mapToRestaurant(
                    dto = dto,
                    favoriteIds = favoriteIds,
                    targetDate = LocalDate.now(UniCafeMapper.HELSINKI_ZONE),
                    targetTime = LocalTime.now(UniCafeMapper.HELSINKI_ZONE)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cached restaurants", e)
            null
        }
    }

    suspend fun setFavorites(ids: Set<Int>) {
        preferencesRepository.saveFavorites(ids)
    }

    suspend fun setDietaryFilter(filter: DietaryFilter) {
        preferencesRepository.saveDietaryFilter(filter)
    }

    suspend fun toggleFavoriteMeal(mealName: String) {
        preferencesRepository.toggleFavoriteMeal(mealName)
    }

    suspend fun setMyDietPreference(diet: com.example.domain.model.MyDietPreference) {
        preferencesRepository.setMyDietPreference(diet)
    }

    suspend fun setHideNonMatchingMeals(hide: Boolean) {
        preferencesRepository.setHideNonMatchingMeals(hide)
    }

    suspend fun setCampusPreference(campus: com.example.domain.model.Campus) {
        preferencesRepository.setCampusPreference(campus)
    }

    suspend fun recordEatenMeal(mealName: String, restaurantName: String, priceEur: Double = 3.10, badges: List<String> = emptyList()) {
        preferencesRepository.recordEatenMeal(mealName, restaurantName, priceEur, badges)
    }

    suspend fun deleteEatenMeal(recordId: String) {
        preferencesRepository.deleteEatenMeal(recordId)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
    }
}
