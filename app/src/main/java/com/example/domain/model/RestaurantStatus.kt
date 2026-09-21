package com.example.domain.model

data class RestaurantStatus(
    val isOpenNow: Boolean,
    val displayText: String,
    val shortStatus: String,
    val hoursDescription: String,
    val countdownText: String? = null,
    val lunchEndTime: String? = null,
    val nextOpeningText: String? = null
) {
    companion object {
        val CLOSED_TODAY = RestaurantStatus(
            isOpenNow = false,
            displayText = "CLOSED • Closed today",
            shortStatus = "CLOSED",
            hoursDescription = "Closed today",
            countdownText = "Closed today",
            nextOpeningText = "Closed today"
        )
    }
}
