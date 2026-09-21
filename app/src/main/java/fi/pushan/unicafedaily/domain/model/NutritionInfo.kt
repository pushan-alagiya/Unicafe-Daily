package fi.pushan.unicafedaily.domain.model

data class NutritionInfo(
    val caloriesKcal: String?,
    val energyKj: String?,
    val protein: String?,
    val fat: String?,
    val saturatedFat: String?,
    val carbs: String?,
    val sugars: String?,
    val fiber: String?,
    val salt: String?,
    val lactose: String?
) {
    val hasMacros: Boolean
        get() = caloriesKcal != null || protein != null || carbs != null || fat != null

    companion object {
        fun parse(raw: String?): NutritionInfo? {
            if (raw.isNullOrBlank()) return null

            fun extract(pattern: String): String? {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                val match = regex.find(raw) ?: return null
                val value = match.groupValues.getOrNull(1)?.replace(",", ".")?.trim()
                return value?.takeIf { it.isNotEmpty() }
            }

            val kcal = extract("""(\d+(?:[.,]\d+)?)\s*kcal""")?.let { "$it kcal" }
            val kj = extract("""(\d+(?:[.,]\d+)?)\s*kJ""")?.let { "$it kJ" }
            val fat = extract("""(?:rasva|fat|fett)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val satFat = extract("""(?:tyydyttynyttä|saturated|mättat)\s*(?:rasvaa|fat|fett)?\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val carbs = extract("""(?:hiilihydraat(?:it)?|carbohydrate(?:s)?|carbs|kolhydrat(?:er)?)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val sugars = extract("""(?:sokereita|sokeri|sugars?|socker(?:arter)?)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val protein = extract("""(?:proteiini(?:a)?|protein)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val salt = extract("""(?:suola(?:a)?|salt)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val fiber = extract("""(?:ravintokuitu(?:a)?|kuitu|fibre|fiber|kostfiber)\s+(\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }
            val lactose = extract("""(?:laktoosi(?:a)?|lactose|laktos)\s+(<?\d+(?:[.,]\d+)?)\s*g""")?.let { "${it}g" }

            return NutritionInfo(
                caloriesKcal = kcal,
                energyKj = kj,
                protein = protein,
                fat = fat,
                saturatedFat = satFat,
                carbs = carbs,
                sugars = sugars,
                fiber = fiber,
                salt = salt,
                lactose = lactose
            )
        }
    }
}
