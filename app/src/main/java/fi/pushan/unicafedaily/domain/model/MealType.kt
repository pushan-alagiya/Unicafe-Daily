package fi.pushan.unicafedaily.domain.model

enum class MealType(val displayName: String) {
    VEGAN("Vegan / Plant-based"),
    SOUP("Warm Soup"),
    PASTA("Pasta & Noodles"),
    FISH("Fish & Seafood"),
    CHICKEN("Chicken & Poultry"),
    MEAT("Meat Classic"),
    BURGER_GRILL("Burger & Grill"),
    SALAD("Fresh Salad"),
    DESSERT("Sweet & Bakery"),
    CHEF_SPECIAL("Chef's Special");

    companion object {
        fun detect(name: String, category: String, badges: List<String>): MealType {
            val text = "${name.lowercase()} ${category.lowercase()}"
            val isVeganBadge = badges.any { it.equals("Veg", ignoreCase = true) }

            return when {
                text.contains("keitto") || text.contains("soppa") || text.contains("soup") -> SOUP
                text.contains("kala") || text.contains("lohi") || text.contains("seiti") ||
                        text.contains("turska") || text.contains("tonnikala") || text.contains("fish") -> FISH
                text.contains("kana") || text.contains("broileri") || text.contains("kalkkuna") || text.contains("chicken") -> CHICKEN
                text.contains("burger") || text.contains("hampurilainen") || text.contains("grilli") ||
                        text.contains("ranskalai") || text.contains("pihvi") && !isVeganBadge -> BURGER_GRILL
                text.contains("pasta") || text.contains("spagetti") || text.contains("lasagne") ||
                        text.contains("makaroni") || text.contains("nuudeli") || text.contains("noodle") -> PASTA
                text.contains("salaatti") || text.contains("salad") -> SALAD
                text.contains("jälkiruoka") || text.contains("kakku") || text.contains("mousse") ||
                        text.contains("pull") || text.contains("rahka") || text.contains("dessert") -> DESSERT
                isVeganBadge || text.contains("vegaani") || text.contains("kasvis") ||
                        text.contains("tofu") || text.contains("härkis") || text.contains("kaura") -> VEGAN
                text.contains("porsas") || text.contains("nauta") || text.contains("liha") ||
                        text.contains("kinkku") || text.contains("makkara") || text.contains("meat") -> MEAT
                else -> CHEF_SPECIAL
            }
        }
    }
}
