package com.example.domain.model

import java.util.UUID

data class EatenMealRecord(
    val id: String = UUID.randomUUID().toString(),
    val mealName: String,
    val restaurantName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priceEur: Double = 3.10,
    val dateString: String = "",
    val dietaryBadges: List<String> = emptyList()
)

data class RecentlyViewedDish(
    val id: String = UUID.randomUUID().toString(),
    val mealName: String,
    val restaurantName: String,
    val category: String = "",
    val studentPrice: String? = null,
    val dietaryBadges: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class MyDietPreference(val displayName: String, val badgeKey: String?) {
    NONE("None / All", null),
    VEGAN("Vegan (Kasvis)", "Veg"),
    GLUTEN_FREE("Gluten-free (Gluteeniton)", "G"),
    MILK_FREE("Milk-free (Maidoton)", "M")
}

enum class Campus(val displayName: String, val shortName: String, val searchKeyword: String) {
    ALL("All Campuses", "All", ""),
    KUMPULA("Kumpula Campus", "Kumpula", "kumpula"),
    KESKUSTA("City Centre", "Keskusta", "keskust"),
    MEILAHTI("Meilahti Campus", "Meilahti", "meilahti"),
    VIIKKI("Viikki Campus", "Viikki", "viikki")
}
