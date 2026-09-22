package fi.pushan.unicafedaily.domain.model

data class Restaurant(
    val id: Int,
    val name: String,
    val slug: String,
    val campus: String = "",
    val address: String = "",
    val phone: String? = null,
    val email: String? = null,
    val description: String? = null,
    val websiteUrl: String? = null,
    val visitingHours: RestaurantVisitingHours? = null,
    val status: RestaurantStatus = RestaurantStatus.CLOSED_TODAY,
    val todaysMeals: List<Meal> = emptyList(),
    val menuDateDisplay: String? = null,
    val isFavorite: Boolean = false
)
