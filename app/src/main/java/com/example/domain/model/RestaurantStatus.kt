package com.example.domain.model

enum class StatusState {
    OPEN,
    OPENING_SOON,
    CLOSED
}

data class RestaurantStatus(
    val state: StatusState = StatusState.CLOSED,
    val isOpenNow: Boolean = false,
    val displayText: String = "",
    val shortStatus: String = "CLOSED",
    val hoursDescription: String = "",
    val countdownText: String? = null,
    val lunchEndTime: String? = null,
    val nextOpeningText: String? = null
) {
    companion object {
        val CLOSED_TODAY = RestaurantStatus(
            state = StatusState.CLOSED,
            isOpenNow = false,
            displayText = "CLOSED • Closed today",
            shortStatus = "CLOSED",
            hoursDescription = "Closed today",
            countdownText = "Closed today",
            nextOpeningText = "Closed today"
        )
    }
}
