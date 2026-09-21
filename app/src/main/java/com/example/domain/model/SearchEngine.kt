package com.example.domain.model

data class MealSearchMatch(
    val meal: Meal,
    val matchReason: String? = null
)

object SearchEngine {

    /**
     * Searches a restaurant's meals against query across dish name, description,
     * ingredients, category, dietary badges, allergens, carbon footprint, and restaurant name.
     */
    fun matchMeal(meal: Meal, restaurantName: String, query: String): String? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null

        if (meal.name.lowercase().contains(q)) {
            return "Dish name"
        }
        if (meal.category.lowercase().contains(q)) {
            return "Category: ${meal.category}"
        }
        if (meal.ingredients?.lowercase()?.contains(q) == true) {
            val matchingIngredient = meal.parsedIngredients.firstOrNull { it.lowercase().contains(q) }
            return if (matchingIngredient != null) "Ingredient: $matchingIngredient" else "Ingredients"
        }
        if (meal.dietaryBadges.any { it.lowercase() == q || it.lowercase().contains(q) }) {
            val badge = meal.dietaryBadges.firstOrNull { it.lowercase().contains(q) }
            return "Dietary: $badge"
        }
        if (meal.allergens.any { it.lowercase().contains(q) }) {
            val allergen = meal.allergens.firstOrNull { it.lowercase().contains(q) }
            return "Allergen: $allergen"
        }
        if (meal.nutrition?.lowercase()?.contains(q) == true) {
            return "Nutrition"
        }
        if (meal.carbonFootprint?.lowercase()?.contains(q) == true) {
            return "Carbon footprint"
        }
        if (restaurantName.lowercase().contains(q)) {
            return "Restaurant: $restaurantName"
        }

        return null
    }

    fun filterRestaurantsBySearch(
        restaurants: List<Restaurant>,
        query: String,
        selectedFilter: DietaryFilter = DietaryFilter.ALL
    ): List<Restaurant> {
        val trimmed = query.trim()
        return restaurants.mapNotNull { restaurant ->
            val filteredMeals = restaurant.todaysMeals.filter { meal ->
                // Apply dietary filter first
                val matchesDiet = selectedFilter.matches(meal.dietaryBadges)
                if (!matchesDiet) return@filter false

                if (trimmed.isEmpty()) return@filter true

                matchMeal(meal, restaurant.name, trimmed) != null
            }

            if (filteredMeals.isEmpty() && trimmed.isNotEmpty()) {
                null
            } else {
                restaurant.copy(todaysMeals = filteredMeals)
            }
        }
    }
}
