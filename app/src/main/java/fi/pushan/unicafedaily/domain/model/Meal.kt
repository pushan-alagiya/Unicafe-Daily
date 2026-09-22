package fi.pushan.unicafedaily.domain.model

data class Meal(
    val id: String,
    val name: String,
    val category: String,
    val studentPrice: String?,
    val normalPrice: String?,
    val graduatePrice: String? = null,
    val staffPrice: String? = null,
    val dietaryBadges: List<String>,
    val allergens: List<String>,
    val ingredients: String?,
    val nutrition: String?,
    val carbonFootprint: String?,
    val nutritionInfo: NutritionInfo? = null,
    val mealType: MealType = MealType.CHEF_SPECIAL,
    val parsedIngredients: List<String> = emptyList()
) {
    fun priceForCategory(customerCategory: CustomerCategory): String? {
        return when (customerCategory) {
            CustomerCategory.STUDENT -> studentPrice
            CustomerCategory.GRADUATE -> graduatePrice ?: studentPrice
            CustomerCategory.STAFF -> staffPrice ?: normalPrice
            CustomerCategory.NORMAL -> normalPrice ?: studentPrice
        }
    }
}
