package fi.pushan.unicafedaily.domain.model

data class Restaurant(
    val id: Int,
    val name: String,
    val slug: String,
    val campus: String,
    val address: String,
    val phone: String?,
    val email: String?,
    val visitingHours: RestaurantVisitingHours?,
    val status: RestaurantStatus,
    val todaysMeals: List<Meal>,
    val menuDateDisplay: String?,
    val isFavorite: Boolean = false
)
