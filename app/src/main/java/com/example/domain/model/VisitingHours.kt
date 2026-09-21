package com.example.domain.model

data class TimeSchedule(
    val label: String,
    val hours: String,
    val isClosed: Boolean = false
)

data class RestaurantVisitingHours(
    val lunchSchedules: List<TimeSchedule>,
    val businessSchedules: List<TimeSchedule>
)
