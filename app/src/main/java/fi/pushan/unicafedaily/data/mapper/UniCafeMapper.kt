package fi.pushan.unicafedaily.data.mapper

import fi.pushan.unicafedaily.data.dto.DailyMenuDto
import fi.pushan.unicafedaily.data.dto.MealDto
import fi.pushan.unicafedaily.data.dto.RestaurantDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursItemDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursSectionDto
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.domain.model.RestaurantStatus
import fi.pushan.unicafedaily.domain.model.RestaurantVisitingHours
import fi.pushan.unicafedaily.domain.model.TimeSchedule
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
            address = dto.menuData?.address?.trim()?.takeIf { it.isNotBlank() } ?: (dto.address?.trim() ?: ""),
            phone = dto.menuData?.phone?.trim(),
            email = dto.menuData?.email?.trim(),
            description = dto.menuData?.description?.trim(),
            websiteUrl = dto.permalink?.trim()?.takeIf { it.isNotBlank() } ?: if (!dto.slug.isNullOrBlank()) "https://unicafe.fi/en/restaurants/${dto.slug}/" else "https://unicafe.fi",
            visitingHours = visitingHours,
            status = status,
            todaysMeals = todaysMeals,
            menuDateDisplay = todayMenu?.date,
            isFavorite = isFav
        )
    }

    fun mapToMeal(dto: MealDto, id: String): Meal {
        val categoryName = dto.price?.name?.trim() ?: "Lounas"
        val lowerCat = categoryName.lowercase(Locale.ROOT)
        val lowerName = dto.name.lowercase(Locale.ROOT)

        val isSpecial = lowerCat.contains("erikois") || lowerCat.contains("special") ||
                lowerName.contains("päivän erikoinen") || lowerCat.contains("today's special")
        val isBuffet = lowerCat.contains("buffet") || lowerCat.contains("noutopöytä")
        val isBreakfast = lowerCat.contains("aamiainen") || lowerCat.contains("breakfast") || lowerCat.contains("frukost")

        val defaultStudent = when {
            isSpecial -> "€5.30"
            isBuffet -> "€10.50"
            isBreakfast -> "€4.00"
            else -> "€3.10"
        }
        val defaultGraduate = when {
            isSpecial -> "€8.75"
            isBuffet -> "€11.00"
            isBreakfast -> "€4.50"
            else -> "€6.35"
        }
        val defaultStaff = when {
            isSpecial -> "€8.90"
            isBuffet -> "€11.00"
            isBreakfast -> "€4.50"
            else -> "€7.30"
        }
        val defaultNormal = when {
            isSpecial -> "€11.50"
            isBuffet -> "€13.00"
            isBreakfast -> "€4.50"
            else -> "€9.80"
        }

        fun formatPrice(raw: String?, default: String): String {
            if (raw.isNullOrBlank()) return default
            val clean = raw.trim().replace(",", ".")
            return if (clean.startsWith("€")) clean else "€$clean"
        }

        val formattedStudentPrice = formatPrice(dto.price?.value?.student ?: dto.price?.value?.studentHyy, defaultStudent)
        val formattedGraduatePrice = formatPrice(dto.price?.value?.graduate ?: dto.price?.value?.graduateHyy, defaultGraduate)
        val formattedStaffPrice = formatPrice(dto.price?.value?.contract, defaultStaff)
        val formattedNormalPrice = formatPrice(dto.price?.value?.normal, defaultNormal)

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

        val parsedNutrition = fi.pushan.unicafedaily.domain.model.NutritionInfo.parse(dto.nutrition)
        val detectedMealType = fi.pushan.unicafedaily.domain.model.MealType.detect(dto.name, categoryName, dietaryBadges)
        val parsedIngredients = parseIngredients(ingredientsCleaned)

        return Meal(
            id = id,
            name = dto.name.trim(),
            category = categoryName,
            studentPrice = formattedStudentPrice,
            normalPrice = formattedNormalPrice,
            graduatePrice = formattedGraduatePrice,
            staffPrice = formattedStaffPrice,
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
                state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • Hours unavailable",
                shortStatus = "CLOSED",
                hoursDescription = "Hours unavailable"
            )
        }

        val lunchSection = visitingHours.lounas
        val businessSection = visitingHours.business

        val allItems = (lunchSection?.items.orEmpty() + businessSection?.items.orEmpty()).distinct()
        if (allItems.isEmpty()) {
            return RestaurantStatus(
                state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
                isOpenNow = false,
                displayText = "CLOSED • No service hours",
                shortStatus = "CLOSED",
                hoursDescription = "No service hours"
            )
        }

        val dayOfWeek = targetDate.dayOfWeek

        // 1. Check for seasonal/holiday exceptions ONLY if today falls inside that exception date range
        val activeException = allItems.firstOrNull { item ->
            isDateInExceptionRange(item.label, targetDate)
        }

        if (activeException != null) {
            val isClosedException = activeException.closedException == true ||
                    activeException.hours?.contains("suljettu", ignoreCase = true) == true ||
                    activeException.hours?.contains("closed", ignoreCase = true) == true ||
                    activeException.hours?.contains("stängt", ignoreCase = true) == true
            if (isClosedException) {
                val nextOpeningDesc = findNextOpening(allItems, dayOfWeek)
                val nextText = if (nextOpeningDesc != null) " • $nextOpeningDesc" else ""
                return RestaurantStatus(
                    state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
                    isOpenNow = false,
                    displayText = "CLOSED • Seasonally closed$nextText",
                    shortStatus = "CLOSED",
                    hoursDescription = "Closed (${activeException.label ?: "Seasonal"})",
                    countdownText = "Closed today",
                    nextOpeningText = nextOpeningDesc
                )
            }
        }

        val nextOpeningDesc = findNextOpening(allItems, dayOfWeek)

        // 2. Find today's active schedule item (prefer lunch schedule, fallback to business schedule)
        val todayScheduleItem = (lunchSection?.items?.firstOrNull { item ->
            !isDateInExceptionRange(item.label, targetDate) && matchesDayOfWeek(item.label, dayOfWeek)
        } ?: businessSection?.items?.firstOrNull { item ->
            !isDateInExceptionRange(item.label, targetDate) && matchesDayOfWeek(item.label, dayOfWeek)
        })

        val isExplicitlyClosed = todayScheduleItem?.closedException == true ||
                todayScheduleItem?.hours?.contains("suljettu", ignoreCase = true) == true ||
                todayScheduleItem?.hours?.contains("closed", ignoreCase = true) == true ||
                todayScheduleItem?.hours?.contains("stängt", ignoreCase = true) == true

        if (todayScheduleItem == null || isExplicitlyClosed) {
            val nextText = if (nextOpeningDesc != null) " • $nextOpeningDesc" else ""
            return RestaurantStatus(
                state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
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
                state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
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
            val state = if (isOpeningSoon) fi.pushan.unicafedaily.domain.model.StatusState.OPENING_SOON else fi.pushan.unicafedaily.domain.model.StatusState.CLOSED

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
                state = fi.pushan.unicafedaily.domain.model.StatusState.OPEN,
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
                state = fi.pushan.unicafedaily.domain.model.StatusState.CLOSED,
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
        for (offset in 1..7) {
            val checkDay = currentDayOfWeek.plus(offset.toLong())
            val matchingItem = items.firstOrNull { item ->
                item.closedException != true &&
                        item.hours?.contains("suljettu", ignoreCase = true) != true &&
                        item.hours?.contains("closed", ignoreCase = true) != true &&
                        item.hours?.contains("stängt", ignoreCase = true) != true &&
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
                isClosed = it.closedException == true ||
                        it.hours?.contains("suljettu", ignoreCase = true) == true ||
                        it.hours?.contains("closed", ignoreCase = true) == true ||
                        it.hours?.contains("stängt", ignoreCase = true) == true
            )
        } ?: emptyList()

        val businessList = dto.business?.items?.map {
            TimeSchedule(
                label = it.label ?: "",
                hours = it.hours ?: "",
                isClosed = it.closedException == true ||
                        it.hours?.contains("suljettu", ignoreCase = true) == true ||
                        it.hours?.contains("closed", ignoreCase = true) == true ||
                        it.hours?.contains("stängt", ignoreCase = true) == true
            )
        } ?: emptyList()

        return RestaurantVisitingHours(
            lunchSchedules = lunchList,
            businessSchedules = businessList
        )
    }

    /**
     * Checks if a weekday label in Finnish, English, or Swedish matches a given DayOfWeek.
     * Examples of labels in UniCafe API:
     * "Ma–Pe", "Ma-Pe", "Mon–Fri", "Mon-Fri", "Mån–Fre", "Mon–Thu", "Ma–To",
     * "La–Su", "Sat–Sun", "Lör–Sön", "Pe", "Fri", "Fre", "La", "Sat", "Lör", "Su", "Sun", "Sön"
     */
    fun matchesDayOfWeek(label: String?, dayOfWeek: DayOfWeek): Boolean {
        if (label.isNullOrBlank()) return false
        val normalized = label.replace("–", "-")
            .replace("—", "-")
            .replace(" ", "")
            .trim()
            .lowercase(Locale.ROOT)

        // Mon-Fri / Ma-Pe / Mån-Fre
        if (normalized.contains("ma-pe") || normalized.contains("mon-fri") || normalized.contains("mån-fre")) {
            return dayOfWeek in listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
        }

        // Mon-Thu / Ma-To / Mån-Tors
        if (normalized.contains("ma-to") || normalized.contains("mon-thu") ||
            normalized.contains("mån-tors") || normalized.contains("mån-tor") ||
            normalized.contains("mon-thurs")) {
            return dayOfWeek in listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY
            )
        }

        // Sat-Sun / La-Su / Lör-Sön
        if (normalized.contains("la-su") || normalized.contains("sat-sun") || normalized.contains("lör-sön")) {
            return dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }

        // Mon-Sun / Ma-Su / Mån-Sön
        if (normalized.contains("ma-su") || normalized.contains("mon-sun") || normalized.contains("mån-sön")) {
            return true
        }

        // Mon-Sat / Ma-La / Mån-Lör
        if (normalized.contains("ma-la") || normalized.contains("mon-sat") || normalized.contains("mån-lör")) {
            return dayOfWeek != DayOfWeek.SUNDAY
        }

        // Single days
        val isMon = normalized == "ma" || normalized == "mon" || normalized == "mån" ||
                normalized == "maanantai" || normalized == "monday" || normalized == "måndag"
        val isTue = normalized == "ti" || normalized == "tue" || normalized == "tis" ||
                normalized == "tiistai" || normalized == "tuesday" || normalized == "tisdag"
        val isWed = normalized == "ke" || normalized == "wed" || normalized == "ons" ||
                normalized == "keskiviikko" || normalized == "wednesday" || normalized == "onsdag"
        val isThu = normalized == "to" || normalized == "thu" || normalized == "tors" || normalized == "tor" ||
                normalized == "torstai" || normalized == "thursday" || normalized == "torsdag"
        val isFri = normalized == "pe" || normalized == "fri" || normalized == "fre" ||
                normalized == "perjantai" || normalized == "friday" || normalized == "fredag"
        val isSat = normalized == "la" || normalized == "sat" || normalized == "lör" ||
                normalized == "lauantai" || normalized == "saturday" || normalized == "lördag"
        val isSun = normalized == "su" || normalized == "sun" || normalized == "sön" ||
                normalized == "sunnuntai" || normalized == "sunday" || normalized == "söndag"

        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> isMon
            DayOfWeek.TUESDAY -> isTue
            DayOfWeek.WEDNESDAY -> isWed
            DayOfWeek.THURSDAY -> isThu
            DayOfWeek.FRIDAY -> isFri
            DayOfWeek.SATURDAY -> isSat
            DayOfWeek.SUNDAY -> isSun
        }
    }

    /**
     * Checks if targetDate falls within a seasonal date range or single date exception.
     * Examples: "15.6.-31.12.", "15.6.-23.8.", "1.-12.6.", "4.5.-16.8.", "24.12."
     */
    fun isDateInExceptionRange(label: String?, targetDate: LocalDate): Boolean {
        if (label.isNullOrBlank()) return false
        val clean = label.replace("–", "-")
            .replace("—", "-")
            .replace(" ", "")
            .trim()
        val day = targetDate.dayOfMonth
        val month = targetDate.monthValue
        val curVal = month * 100 + day

        try {
            if (clean.contains("-")) {
                val parts = clean.split("-")
                if (parts.size == 2) {
                    val endTokens = parts[1].trim().split(".").filter { it.isNotBlank() }
                    if (endTokens.size >= 2) {
                        val endDay = endTokens[0].toIntOrNull() ?: return false
                        val endMonth = endTokens[1].toIntOrNull() ?: return false
                        val endVal = endMonth * 100 + endDay

                        val startTokens = parts[0].trim().split(".").filter { it.isNotBlank() }
                        val startDay = startTokens[0].toIntOrNull() ?: return false
                        val startMonth = if (startTokens.size >= 2) {
                            startTokens[1].toIntOrNull() ?: endMonth
                        } else {
                            endMonth // e.g. "1.-12.6." -> start month is 6 (same as end month)
                        }
                        val startVal = startMonth * 100 + startDay

                        return if (startVal <= endVal) {
                            curVal in startVal..endVal
                        } else {
                            // Span across year-end (e.g. 20.12.-6.1.)
                            curVal >= startVal || curVal <= endVal
                        }
                    }
                }
            } else {
                // Single date like "24.12."
                val tokens = clean.split(".").filter { it.isNotBlank() }
                if (tokens.size >= 2) {
                    val exDay = tokens[0].toIntOrNull()
                    val exMonth = tokens[1].toIntOrNull()
                    return exDay == day && exMonth == month
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
