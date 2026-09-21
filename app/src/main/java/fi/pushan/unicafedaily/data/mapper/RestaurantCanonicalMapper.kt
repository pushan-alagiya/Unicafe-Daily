package fi.pushan.unicafedaily.data.mapper

object RestaurantCanonicalMapper {

    // Map of restaurant ID -> Canonical ID (using Finnish IDs as primary canonical IDs)
    private val ID_TO_CANONICAL_MAP = mapOf(
        // Kaivopiha
        2543 to 2543, 2558 to 2543, 2559 to 2543,
        // Exactum
        1356 to 1356, 2500 to 1356, 2505 to 1356,
        // Chemicum
        1354 to 1354, 2498 to 1354, 2503 to 1354,
        // Biokeskus
        1335 to 1335, 2501 to 1335, 2502 to 1335,
        // Biokeskus 2
        4573 to 4573, 4575 to 4573, 4574 to 4573,
        // Infokeskus
        1357 to 1357, 2506 to 1357, 2507 to 1357,
        // Infokeskus alakerta / student canteen
        4516 to 4516, 4518 to 4516, 4519 to 4516,
        // Meilahti
        1358 to 1358, 2508 to 1358, 2509 to 1358,
        // Metsätalo
        1359 to 1359, 2583 to 1359, 2584 to 1359,
        // Olivia
        1360 to 1360, 2510 to 1360, 2512 to 1360,
        // Olivia henkilöstöravintola
        5588 to 5588, 5589 to 5588, 5590 to 5588,
        // Topelias
        1362 to 1362, 2696 to 1362, 2698 to 1362,
        // Physicum
        1363 to 1363, 2591 to 1363, 2592 to 1363,
        // Porthania
        1364 to 1364, 2514 to 1364, 2515 to 1364,
        // Porthania Opettajat
        1365 to 1365, 2599 to 1365, 2821 to 1365,
        // Soc&Kom
        1372 to 1372, 2511 to 1372, 2513 to 1372,
        // Viikuna
        1374 to 1374, 2589 to 1374, 2590 to 1374,
        // Kaisa-talo
        1375 to 1375, 4383 to 1375, 4384 to 1375,
        // Portaali
        1353 to 1353, 2496 to 1353, 2497 to 1353,
        // Chemicum Opettajat
        1355 to 1355, 2499 to 1355, 2504 to 1355,
        // Myöhä
        4377 to 4377, 4379 to 4377, 4380 to 4377,
        // Tähkä
        4576 to 4576, 4577 to 4576, 4578 to 4576,
        // Terkko
        5223 to 5223, 5225 to 5223, 5224 to 5223,
        // Serpens
        5391 to 5391, 5393 to 5391, 5392 to 5391
    )

    // Map of Slug -> Canonical ID
    private val SLUG_TO_CANONICAL_MAP = mapOf(
        "kaivopiha" to 2543,
        "exactum" to 1356,
        "chemicum" to 1354,
        "biokeskus" to 1335,
        "biokeskus-3" to 4573,
        "infokeskus" to 1357,
        "infokeskus-alakerta" to 4516,
        "infokeskus-student-canteen" to 4516,
        "infokeskus-studentmatsal" to 4516,
        "meilahti" to 1358,
        "metsatalo" to 1359,
        "olivia" to 1360,
        "olivia-henkilostoravintola" to 5588,
        "pesco-vege-topelias" to 1362,
        "physicum" to 1363,
        "porthania" to 1364,
        "porthania-opettajien-ravintola" to 1365,
        "sockom" to 1372,
        "viikuna" to 1374,
        "well-kaisa-talo" to 1375,
        "cafe-portaali" to 1353,
        "chemicum-opettajien-ravintola" to 1355,
        "chemicum-teachers-restaurant" to 1355,
        "myoha" to 4377,
        "tahka" to 4576,
        "terkko" to 5223,
        "serpens" to 5391
    )

    fun getCanonicalId(id: Int, slug: String? = null): Int {
        ID_TO_CANONICAL_MAP[id]?.let { return it }
        if (!slug.isNullOrBlank()) {
            SLUG_TO_CANONICAL_MAP[slug.lowercase()]?.let { return it }
        }
        return id
    }

    fun isSameRestaurant(id1: Int, id2: Int, slug1: String? = null, slug2: String? = null): Boolean {
        if (id1 == id2) return true
        val canonical1 = getCanonicalId(id1, slug1)
        val canonical2 = getCanonicalId(id2, slug2)
        return canonical1 == canonical2
    }
}
