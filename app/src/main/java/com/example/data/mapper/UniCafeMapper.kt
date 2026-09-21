package com.example.data.mapper

import com.example.data.dto.DailyMenuDto
import com.example.data.dto.MealDto
import com.example.data.dto.RestaurantDto
import com.example.data.dto.VisitingHoursDto
import com.example.data.dto.VisitingHoursItemDto
import com.example.data.dto.VisitingHoursSectionDto
import com.example.domain.model.Meal
import com.example.domain.model.Restaurant
import com.example.domain.model.RestaurantStatus
import com.example.domain.model.RestaurantVisitingHours
import com.example.domain.model.TimeSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

object UniCafeMapper {

    val HELSINKI_ZONE: ZoneId = ZoneId.of("Europe/Helsinki")

    fun mapToRestaurant(
        dto: RestaurantDto,
        favoriteIds: Set<Int>,
        targetDate: LocalDate = LocalDate.now(HELSINKI_ZONE),
        targetTime: LocalTime = LocalTime.now(HELSINKI_ZONE)
    ): Restaurant {
        val campusName = dto.location?.firstOrNull()?.name ?: "Helsinki"
        val visitingHours = mapVisitingHours(dto.menuData?.visitingHours)
        val status = calculateStatus(dto.menuData?.visitingHours, targetDate, targetTime)

        // Find menu for target date
        val todayMenu = findMenuForDate(dto.menuData?.menus, targetDate)
        val todaysMeals = todayMenu?.data?.mapIndexed { index, mealDto ->
            mapToMeal(mealDto, "${dto.id}_${index}")
        } ?: emptyList()

        val canonicalFavIds = favoriteIds.map { RestaurantCanonicalMapper.getCanonicalId(it) }.toSet()
        val dtoCanonicalId = RestaurantCanonicalMapper.getCanonicalId(dto.id, dto.slug)
        val isFav = canonicalFavIds.contains(dtoCanonicalId)

        return Restaurant(
            id = dto.id,
            name = dto.title.trim(),
            slug = dto.slug ?: "restaurant-${dto.id}",
            campus = campusName,
            address = dto.address?.trim() ?: (dto.menuData?.address?.trim() ?: ""),
            phone = dto.menuData?.phone?.trim(),
            email = dto.menuData?.email?.trim(),
            visitingHours = visitingHours,
            status = status,
            todaysMeals = todaysMeals,
            menuDateDisplay = todayMenu?.date,
            isFavorite = isFav
        )
    }

    fun mapToMeal(dto: MealDto, id: String): Meal {
        val studentPriceRaw = dto.price?.value?.student ?: dto.price?.value?.studentHyy
        val normalPriceRaw = dto.price?.value?.normal

        val formattedStudentPrice = studentPriceRaw?.takeIf { it.isNotBlank() }?.let {
            if (it.startsWith("€")) it else "€$it"
        }
        val formattedNormalPrice = normalPriceRaw?.takeIf { it.isNotBlank() }?.let {
            if (it.startsWith("€")) it else "€$it"
        }

        val rawBadges = dto.meta?.get("0") ?: emptyList()
        val dietaryBadges = rawBadges.map { it.trim() }.filter { it.isNotEmpty() }

        val rawAllergens = dto.meta?.get("1") ?: emptyList()
        val allergens = rawAllergens.map { it.trim() }.filter { it.isNotEmpty() }

        // Extract carbon footprint from ingredients
        var carbonFootprint: String? = null
        var ingredientsCleaned = dto.ingredients?.trim()
        if (ingredientsCleaned != null) {
            val keywords = listOf("Hiilijalanjälki:", "Carbon footprint:", "Koldioxidavtryck:")
            for (kw in keywords) {
                if (ingredientsCleaned!!.contains(kw, ignoreCase = true)) {
                    val idx = ingredientsCleaned!!.indexOf(kw, ignoreCase = true)
                    carbonFootprint = ingredientsCleaned!!.substring(idx).trim()
                    ingredientsCleaned = ingredientsCleaned!!.substring(0, idx).trim().trimEnd('.', ',')
                    break
                }
            }
        }

        val parsedNutrition = com.example.domain.model.NutritionInfo.parse(dto.nutrition)
        val categoryName = dto.price?.name?.trim() ?: "Lounas"
        val detectedMealType = com.example.domain.model.MealType.detect(dto.name, categoryName, dietaryBadges)
        val parsedIngredients = parseIngredients(ingredientsCleaned)

        return Meal(
            id = id,
            name = dto.name.trim(),
            category = categoryName,
            studentPrice = formattedStudentPrice,
            normalPrice = formattedNormalPrice,
            dietaryBadges = dietaryBadges,
            allergens = allergens,
            ingredients = ingredientsCleaned,
            nutrition = dto.nutrition?.trim(),
            carbonFootprint = carbonFootprint,
            nutritionInfo = parsedNutrition,
            mealType = detectedMealType,
            parsedIngredients = parsedIngredients
        )
    }

    private fun parseIngredients(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        // Split by commas outside of parentheses or simple clean split
        val list = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        for (char in text) {
            when (char) {
                '(' -> { parenDepth++; current.append(char) }
                ')' -> { if (parenDepth > 0) parenDepth--; current.append(char) }
                ',' -> {
                    if (parenDepth == 0) {
                        val token = current.toString().trim()
                        if (token.isNotEmpty()) list.add(token)
                        current.clear()
                    } else {
                        current.append(char)
                    }
                }
                else -> current.append(char)
            }
        }
        val lastToken = current.toString().trim().trimEnd('.')
        if (lastToken.isNotEmpty()) list.add(lastToken)
        return list.take(15) // Clean, scannable list
    }

    fun findMenuForDate(menus: List<DailyMenuDto>?, date: LocalDate): DailyMenuDto? {
        if (menus.isNullOrEmpty()) return null

        val day = date.dayOfMonth
        val month = date.monthValue
        val dayMonthPadded = String.format(Locale.ROOT, "%02d.%02d.", day, month) // e.g. "21.09."
        val dayMonthPlain = "$day.$month." // e.g. "21.9."
        val dayMonthNoTrailing = String.format(Locale.ROOT, "%02d.%02d", day, month) // e.g. "21.09"

        return menus.firstOrNull { menu ->
            menu.date.contains(dayMonthPadded) ||
                    menu.date.contains(dayMonthPlain) ||
                    menu.date.contains(dayMonthNoTrailing)
        }
    }

    fun calculateStatus(
        visitingHours: VisitingHoursDto?,
        targetDate: LocalDate = LocalDate.now(HELSINKI_ZONE),
        targetTime: LocalTime = LocalTime.now(HELSINKI_ZONE)
    ): RestaurantStatus {
        if (visitingHours == null) {
            return RestaurantStatus(
                state = com.example.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • Hours unavailable",
                shortStatus = "CLOSED",
                hoursDescription = "Hours unavailable"
            )
        }

        // Check lunch hours first as UniCafe is primarily a student lunch provider
        val lunchSection = visitingHours.lounas
        val businessSection = visitingHours.business

        val sectionToEvaluate = lunchSection ?: businessSection
        if (sectionToEvaluate == null || sectionToEvaluate.items.isNullOrEmpty()) {
            return RestaurantStatus(
                state = com.example.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • No lunch service",
                shortStatus = "CLOSED",
                hoursDescription = "No lunch service"
            )
        }

        val dayOfWeek = targetDate.dayOfWeek
        val dayOfMonth = targetDate.dayOfMonth
        val month = targetDate.monthValue

        // 1. Check for temporary date exceptions like "15.6.-31.12." or exception == true
        val exceptionItem = sectionToEvaluate.items.firstOrNull { item ->
            item.exception == true || isDateInExceptionRange(item.label, dayOfMonth, month)
        }

        if (exceptionItem != null) {
            val isClosedException = exceptionItem.closedException == true ||
                    exceptionItem.hours?.contains("suljettu", ignoreCase = true) == true
            if (isClosedException) {
                return RestaurantStatus(
                    state = com.example.domain.model.StatusState.CLOSED,
                    isOpenNow = false,
                    displayText = "CLOSED • Seasonally closed",
                    shortStatus = "CLOSED",
                    hoursDescription = "Closed (${exceptionItem.label ?: "Seasonal"})"
                )
            }
        }

        val nextOpeningDesc = findNextOpening(sectionToEvaluate.items, dayOfWeek)

        val todayScheduleItem = sectionToEvaluate.items.firstOrNull { item ->
            item.exception != true && matchesDayOfWeek(item.label, dayOfWeek)
        }

        if (todayScheduleItem == null || todayScheduleItem.closedException == true || todayScheduleItem.hours?.contains("suljettu", ignoreCase = true) == true) {
            val nextText = if (nextOpeningDesc != null) " • $nextOpeningDesc" else ""
            return RestaurantStatus(
                state = com.example.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • Closed today$nextText",
                shortStatus = "CLOSED",
                hoursDescription = "Closed today",
                countdownText = "Closed today",
                nextOpeningText = nextOpeningDesc
            )
        }

        val hoursStr = todayScheduleItem.hours?.trim() ?: ""

        // 3. Parse opening hours range (e.g. "11:00-14:00" or "10.30-15.00" or "11:00–13:30")
        val parsedTimes = parseTimeRange(hoursStr)
        if (parsedTimes == null) {
            return RestaurantStatus(
                state = com.example.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • $hoursStr",
                shortStatus = "CLOSED",
                hoursDescription = hoursStr,
                countdownText = hoursStr,
                nextOpeningText = nextOpeningDesc
            )
        }

        val (startTime, endTime) = parsedTimes
        val hoursDesc = "${formatTime(startTime)} – ${formatTime(endTime)}"
        val formattedEndTime = formatTime(endTime)

        return if (targetTime.isBefore(startTime)) {
            val minutesUntilOpen = java.time.Duration.between(targetTime, startTime).toMinutes().coerceAtLeast(1)
            val h = minutesUntilOpen / 60
            val m = minutesUntilOpen % 60
            val countStr = if (h > 0) "${h}h ${m}m" else "${m} min"
            val isOpeningSoon = minutesUntilOpen <= 60
            val state = if (isOpeningSoon) com.example.domain.model.StatusState.OPENING_SOON else com.example.domain.model.StatusState.CLOSED

            RestaurantStatus(
                state = state,
                isOpenNow = false,
                displayText = if (isOpeningSoon) "OPENING SOON • in $countStr" else "CLOSED • Lunch $hoursDesc",
                shortStatus = if (isOpeningSoon) "OPENING SOON" else "CLOSED",
                hoursDescription = "Lunch $hoursDesc",
                countdownText = if (isOpeningSoon) "in $countStr" else "Opens at ${formatTime(startTime)}",
                lunchEndTime = formattedEndTime,
                nextOpeningText = "Opens today at ${formatTime(startTime)}"
            )
        } else if (!targetTime.isAfter(endTime)) {
            val minutesUntilClose = java.time.Duration.between(targetTime, endTime).toMinutes().coerceAtLeast(1)
            val h = minutesUntilClose / 60
            val m = minutesUntilClose % 60
            val countStr = if (h > 0) "${h}h ${m}m left" else "${m} min left"
            RestaurantStatus(
                state = com.example.domain.model.StatusState.OPEN,
                isOpenNow = true,
                displayText = "OPEN • Lunch until $formattedEndTime",
                shortStatus = "OPEN",
                hoursDescription = "Lunch $hoursDesc",
                countdownText = countStr,
                lunchEndTime = formattedEndTime,
                nextOpeningText = null
            )
        } else {
            RestaurantStatus(
                state = com.example.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • Lunch ended at $formattedEndTime",
                shortStatus = "CLOSED",
                hoursDescription = "Lunch $hoursDesc",
                countdownText = "Lunch ended at $formattedEndTime",
                lunchEndTime = formattedEndTime,
                nextOpeningText = nextOpeningDesc ?: "Closed for today"
            )
        }
    }

    private fun findNextOpening(items: List<VisitingHoursItemDto>?, currentDayOfWeek: DayOfWeek): String? {
        if (items.isNullOrEmpty()) return null
        for (offset in 1..6) {
            val checkDay = currentDayOfWeek.plus(offset.toLong())
            val matchingItem = items.firstOrNull { item ->
                item.exception != true && item.closedException != true &&
                        item.hours?.contains("suljettu", ignoreCase = true) != true &&
                        matchesDayOfWeek(item.label, checkDay)
            }
            if (matchingItem != null) {
                val dayName = when (checkDay) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                }
                val startTime = parseTimeRange(matchingItem.hours ?: "")?.first?.let { formatTime(it) } ?: matchingItem.hours
                return "Opens $dayName $startTime"
            }
        }
        return null
    }

    private fun mapVisitingHours(dto: VisitingHoursDto?): RestaurantVisitingHours? {
        if (dto == null) return null

        val lunchList = dto.lounas?.items?.map {
            TimeSchedule(
                label = it.label ?: "",
                hours = it.hours ?: "",
                isClosed = it.closedException == true || it.hours?.contains("suljettu", ignoreCase = true) == true
            )
        } ?: emptyList()

        val businessList = dto.business?.items?.map {
            TimeSchedule(
                label = it.label ?: "",
                hours = it.hours ?: "",
                isClosed = it.closedException == true || it.hours?.contains("suljettu", ignoreCase = true) == true
            )
        } ?: emptyList()

        return RestaurantVisitingHours(
            lunchSchedules = lunchList,
            businessSchedules = businessList
        )
    }

    /**
     * Checks if a Finnish weekday label matches a given DayOfWeek.
     * Examples of labels in UniCafe API:
     * "Ma–Pe", "Ma-Pe", "Ma–To", "Pe", "La", "La–Su", "Ma-Su", "Ma–La"
     */
    fun matchesDayOfWeek(label: String?, dayOfWeek: DayOfWeek): Boolean {
        if (label.isNullOrBlank()) return false
        val normalized = label.replace("–", "-").trim().lowercase(Locale.ROOT)

        return when {
            normalized.contains("ma-pe") || normalized.contains("ma–pe") -> {
                dayOfWeek in listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            }
            normalized.contains("ma-to") || normalized.contains("ma–to") -> {
                dayOfWeek in listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY
                )
            }
            normalized.contains("la-su") || normalized.contains("la–su") -> {
                dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            }
            normalized.contains("ma-su") || normalized.contains("ma–su") -> {
                true
            }
            normalized.contains("ma-la") || normalized.contains("ma–la") -> {
                dayOfWeek != DayOfWeek.SUNDAY
            }
            normalized == "ma" -> dayOfWeek == DayOfWeek.MONDAY
            normalized == "ti" -> dayOfWeek == DayOfWeek.TUESDAY
            normalized == "ke" -> dayOfWeek == DayOfWeek.WEDNESDAY
            normalized == "to" -> dayOfWeek == DayOfWeek.THURSDAY
            normalized == "pe" -> dayOfWeek == DayOfWeek.FRIDAY
            normalized == "la" -> dayOfWeek == DayOfWeek.SATURDAY
            normalized == "su" -> dayOfWeek == DayOfWeek.SUNDAY
            else -> false
        }
    }

    /**
     * Checks if today's date falls within a seasonal date range like "15.6.-31.12."
     */
    private fun isDateInExceptionRange(label: String?, day: Int, month: Int): Boolean {
        if (label == null || !label.contains("-")) return false
        try {
            val parts = label.replace("–", "-").split("-")
            if (parts.size == 2) {
                val startParts = parts[0].trim().split(".")
                val endParts = parts[1].trim().split(".")
                if (startParts.size >= 2 && endParts.size >= 2) {
                    val startDay = startParts[0].toIntOrNull() ?: return false
                    val startMonth = startParts[1].toIntOrNull() ?: return false
                    val endDay = endParts[0].toIntOrNull() ?: return false
                    val endMonth = endParts[1].toIntOrNull() ?: return false

                    val currentVal = month * 100 + day
                    val startVal = startMonth * 100 + startDay
                    val endVal = endMonth * 100 + endDay

                    return currentVal in startVal..endVal
                }
            }
        } catch (_: Exception) {
            // Ignore malformed range
        }
        return false
    }

    /**
     * Parses time range like "11:00-14:00", "11.00-14.00", "10:30–15:00", "8.00-16.30"
     */
    fun parseTimeRange(hours: String): Pair<LocalTime, LocalTime>? {
        try {
            val clean = hours.replace("–", "-").trim()
            val tokens = clean.split("-")
            if (tokens.size == 2) {
                val start = parseSingleTime(tokens[0].trim())
                val end = parseSingleTime(tokens[1].trim())
                if (start != null && end != null) {
                    return Pair(start, end)
                }
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return null
    }

    private fun parseSingleTime(timeStr: String): LocalTime? {
        val sanitized = timeStr.replace(".", ":").trim()
        val parts = sanitized.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h in 0..23 && m in 0..59) {
                return LocalTime.of(h, m)
            }
        } else if (parts.size == 1) {
            val h = parts[0].toIntOrNull() ?: return null
            if (h in 0..23) {
                return LocalTime.of(h, 0)
            }
        }
        return null
    }

    private fun formatTime(time: LocalTime): String {
        return String.format(Locale.ROOT, "%02d:%02d", time.hour, time.minute)
    }
}
