package fi.pushan.unicafedaily.data.api

import fi.pushan.unicafedaily.data.dto.RestaurantDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UniCafeApi {
    @GET(ApiConfig.RESTAURANTS_ENDPOINT)
    suspend fun getRestaurants(
        @Query("lang") language: String = ApiConfig.DEFAULT_LANGUAGE
    ): List<RestaurantDto>
}
