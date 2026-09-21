package com.example.domain.model

enum class DietaryFilter(val label: String, val badge: String) {
    ALL("All", ""),
    VEG("Veg", "Veg"),
    GLUTEN_FREE("G", "G"),
    MILK_FREE("M", "M");

    fun matches(dietaryBadges: List<String>): Boolean {
        return when (this) {
            ALL -> true
            VEG -> dietaryBadges.any { it.equals("Veg", ignoreCase = true) }
            GLUTEN_FREE -> dietaryBadges.any { it.equals("G", ignoreCase = true) }
            MILK_FREE -> dietaryBadges.any { it.equals("M", ignoreCase = true) }
        }
    }
}
