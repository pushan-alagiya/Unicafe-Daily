package fi.pushan.unicafedaily.data.api

/**
 * UniCafe API Configuration
 *
 * UniCafe's official mobile customer-facing website (https://menu.unicafe.fi/) fetches restaurant
 * menus, opening hours, and dietary metadata directly from WordPress REST API endpoint:
 *
 * Base URL: https://unicafe.fi/
 * Endpoint: wp-json/swiss/v1/restaurants/
 * HTTP Method: GET
 * Query Parameters:
 *   - lang: "fi" (default) or "en"
 *
 * The endpoint is public and unauthenticated.
 */
object ApiConfig {
    const val BASE_URL = "https://unicafe.fi/"
    const val RESTAURANTS_ENDPOINT = "wp-json/swiss/v1/restaurants/"
    const val DEFAULT_LANGUAGE = "fi"

    // Default top favorites for Helsinki University students
    val DEFAULT_FAVORITE_IDS = listOf(
        2543, // Kaivopiha
        1356, // Exactum
        1354  // Chemicum
    )

    const val MAX_FAVORITES = 3
}
