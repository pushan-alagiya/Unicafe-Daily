package fi.pushan.unicafedaily

import fi.pushan.unicafedaily.data.api.UniCafeApiClient
import fi.pushan.unicafedaily.data.dto.DailyMenuDto
import fi.pushan.unicafedaily.data.dto.LocationDto
import fi.pushan.unicafedaily.data.dto.MealDto
import fi.pushan.unicafedaily.data.dto.MealPriceDto
import fi.pushan.unicafedaily.data.dto.MenuDataDto
import fi.pushan.unicafedaily.data.dto.PriceValueDto
import fi.pushan.unicafedaily.data.dto.RestaurantDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursItemDto
import fi.pushan.unicafedaily.data.dto.VisitingHoursSectionDto
import fi.pushan.unicafedaily.data.mapper.UniCafeMapper
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class UniCafeMapperTest {

    @Test
    fun `test matchesDayOfWeek handles Finnish abbreviations correctly`() {
        assertTrue(UniCafeMapper.matchesDayOfWeek("Ma–Pe", DayOfWeek.MONDAY))
        assertTrue(UniCafeMapper.matchesDayOfWeek("Ma–Pe", DayOfWeek.WEDNESDAY))
        assertTrue(UniCafeMapper.matchesDayOfWeek("Ma–Pe", DayOfWeek.FRIDAY))
        assertFalse(UniCafeMapper.matchesDayOfWeek("Ma–Pe", DayOfWeek.SATURDAY))
        assertFalse(UniCafeMapper.matchesDayOfWeek("Ma–Pe", DayOfWeek.SUNDAY))

        assertTrue(UniCafeMapper.matchesDayOfWeek("La", DayOfWeek.SATURDAY))
        assertFalse(UniCafeMapper.matchesDayOfWeek("La", DayOfWeek.SUNDAY))

        assertTrue(UniCafeMapper.matchesDayOfWeek("La–Su", DayOfWeek.SUNDAY))
        assertTrue(UniCafeMapper.matchesDayOfWeek("La–Su", DayOfWeek.SATURDAY))
        assertFalse(UniCafeMapper.matchesDayOfWeek("La–Su", DayOfWeek.FRIDAY))
    }

    @Test
    fun `test parseTimeRange parses standard UniCafe hour formats`() {
        val range1 = UniCafeMapper.parseTimeRange("11:00-14:00")
        assertNotNull(range1)
        assertEquals(LocalTime.of(11, 0), range1?.first)
        assertEquals(LocalTime.of(14, 0), range1?.second)

        val range2 = UniCafeMapper.parseTimeRange("10.30–15.00")
        assertNotNull(range2)
        assertEquals(LocalTime.of(10, 30), range2?.first)
        assertEquals(LocalTime.of(15, 0), range2?.second)

        val range3 = UniCafeMapper.parseTimeRange("11:00–13:30")
        assertNotNull(range3)
        assertEquals(LocalTime.of(11, 0), range3?.first)
        assertEquals(LocalTime.of(13, 30), range3?.second)
    }

    @Test
    fun `test calculateStatus returns OPEN during lunch and CLOSED outside lunch`() {
        val testHours = VisitingHoursDto(
            lounas = VisitingHoursSectionDto(
                items = listOf(
                    VisitingHoursItemDto(label = "Ma–Pe", hours = "11:00-14:00")
                )
            )
        )

        val mondayDate = LocalDate.of(2026, 9, 21) // Monday

        // Monday at 12:00 -> OPEN
        val openStatus = UniCafeMapper.calculateStatus(
            visitingHours = testHours,
            targetDate = mondayDate,
            targetTime = LocalTime.of(12, 0)
        )
        assertTrue(openStatus.isOpenNow)
        assertEquals("OPEN", openStatus.shortStatus)
        assertEquals(fi.pushan.unicafedaily.domain.model.StatusState.OPEN, openStatus.state)

        // Monday at 10:30 (30 min before lunch) -> OPENING SOON
        val openingSoonStatus = UniCafeMapper.calculateStatus(
            visitingHours = testHours,
            targetDate = mondayDate,
            targetTime = LocalTime.of(10, 30)
        )
        assertFalse(openingSoonStatus.isOpenNow)
        assertEquals("OPENING SOON", openingSoonStatus.shortStatus)
        assertEquals(fi.pushan.unicafedaily.domain.model.StatusState.OPENING_SOON, openingSoonStatus.state)

        // Monday at 08:00 (more than 1 hour before lunch) -> CLOSED
        val earlyStatus = UniCafeMapper.calculateStatus(
            visitingHours = testHours,
            targetDate = mondayDate,
            targetTime = LocalTime.of(8, 0)
        )
        assertFalse(earlyStatus.isOpenNow)
        assertEquals("CLOSED", earlyStatus.shortStatus)
        assertEquals(fi.pushan.unicafedaily.domain.model.StatusState.CLOSED, earlyStatus.state)

        // Monday at 14:30 (after lunch) -> CLOSED
        val afterStatus = UniCafeMapper.calculateStatus(
            visitingHours = testHours,
            targetDate = mondayDate,
            targetTime = LocalTime.of(14, 30)
        )
        assertFalse(afterStatus.isOpenNow)
        assertEquals("CLOSED", afterStatus.shortStatus)
        assertEquals(fi.pushan.unicafedaily.domain.model.StatusState.CLOSED, afterStatus.state)

        // Sunday (weekend closed) -> CLOSED
        val sundayDate = LocalDate.of(2026, 9, 27) // Sunday
        val weekendStatus = UniCafeMapper.calculateStatus(
            visitingHours = testHours,
            targetDate = sundayDate,
            targetTime = LocalTime.of(12, 0)
        )
        assertFalse(weekendStatus.isOpenNow)
        assertEquals("CLOSED", weekendStatus.shortStatus)
        assertEquals(fi.pushan.unicafedaily.domain.model.StatusState.CLOSED, weekendStatus.state)
        assertEquals("Closed today", weekendStatus.hoursDescription)
    }

    @Test
    fun `test findMenuForDate finds correct menu from date string`() {
        val menus = listOf(
            DailyMenuDto(date = "Ma 21.09.", data = listOf(MealDto(name = "Lentil Soup"))),
            DailyMenuDto(date = "Ti 22.09.", data = listOf(MealDto(name = "Pasta"))),
            DailyMenuDto(date = "Ke 23.09.", data = listOf(MealDto(name = "Curry")))
        )

        val targetDate = LocalDate.of(2026, 9, 21)
        val matched = UniCafeMapper.findMenuForDate(menus, targetDate)
        assertNotNull(matched)
        assertEquals("Ma 21.09.", matched?.date)
        assertEquals("Lentil Soup", matched?.data?.first()?.name)

        val nonExistentDate = LocalDate.of(2026, 9, 28)
        val notFound = UniCafeMapper.findMenuForDate(menus, nonExistentDate)
        assertNull(notFound)
    }

    @Test
    fun `test DietaryFilter matching rules`() {
        val veganBadges = listOf("Veg", "G", "M", "Ilmastovalinta")
        val meatBadges = listOf("G", "L")
        val lactoseFreeBadges = listOf("VL", "G")

        // ALL matches everything
        assertTrue(DietaryFilter.ALL.matches(veganBadges))
        assertTrue(DietaryFilter.ALL.matches(meatBadges))
        assertTrue(DietaryFilter.ALL.matches(emptyList()))

        // VEG matches only vegan meals
        assertTrue(DietaryFilter.VEG.matches(veganBadges))
        assertFalse(DietaryFilter.VEG.matches(meatBadges))

        // GLUTEN_FREE matches "G"
        assertTrue(DietaryFilter.GLUTEN_FREE.matches(veganBadges))
        assertTrue(DietaryFilter.GLUTEN_FREE.matches(meatBadges))
        assertFalse(DietaryFilter.GLUTEN_FREE.matches(listOf("L", "M")))

        // MILK_FREE matches "M"
        assertTrue(DietaryFilter.MILK_FREE.matches(veganBadges))
        assertFalse(DietaryFilter.MILK_FREE.matches(meatBadges))
        assertFalse(DietaryFilter.MILK_FREE.matches(lactoseFreeBadges))
    }

    @Test
    fun `test Moshi parser handles PHP quirk of empty array for price value`() {
        val jsonWithEmptyArrayPrice = """
            {
                "id": 1356,
                "title": "Exactum",
                "slug": "exactum",
                "location": [{"id": 2, "name": "Kumpula"}],
                "menuData": {
                    "menus": [
                        {
                            "date": "Ma 21.09.",
                            "data": [
                                {
                                    "name": "Vegaaninen punajuuripihvi",
                                    "meta": {
                                        "0": ["Veg", "G", "M"],
                                        "1": ["Soija"]
                                    },
                                    "price": {
                                        "name": "Vegaanilounas",
                                        "value": []
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = UniCafeApiClient.moshi.adapter(RestaurantDto::class.java)
        val restaurantDto = adapter.fromJson(jsonWithEmptyArrayPrice)
        assertNotNull(restaurantDto)
        assertEquals("Exactum", restaurantDto?.title)
        val mealDto = restaurantDto?.menuData?.menus?.first()?.data?.first()
        assertNotNull(mealDto)
        assertNull(mealDto?.price?.value) // Correctly parsed as null instead of throwing JsonDataException
    }

    @Test
    fun `test Moshi parser handles object price value`() {
        val jsonWithObjectPrice = """
            {
                "id": 2543,
                "title": "Kaivopiha",
                "location": [{"id": 1, "name": "Keskusta"}],
                "menuData": {
                    "menus": [
                        {
                            "date": "Ma 21.09.",
                            "data": [
                                {
                                    "name": "Tofu-kasviswokki",
                                    "meta": {
                                        "0": ["Veg", "M"],
                                        "1": ["Gluteeni"]
                                    },
                                    "price": {
                                        "name": "Edullisesti",
                                        "value": {
                                            "student": "3.10",
                                            "normal": "9.50"
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = UniCafeApiClient.moshi.adapter(RestaurantDto::class.java)
        val restaurantDto = adapter.fromJson(jsonWithObjectPrice)
        assertNotNull(restaurantDto)
        assertEquals("3.10", restaurantDto?.menuData?.menus?.first()?.data?.first()?.price?.value?.student)
        assertEquals("9.50", restaurantDto?.menuData?.menus?.first()?.data?.first()?.price?.value?.normal)
    }

    @Test
    fun `test mapToMeal formats student price with euro symbol and extracts metadata`() {
        val mealDto = MealDto(
            name = "Linssikeitto",
            ingredients = "Punaisia linssejä, tomaattia. Hiilijalanjälki: 0.28 kg CO2e / annos",
            meta = mapOf(
                "0" to listOf("Veg", "G", "M"),
                "1" to listOf("Selleri")
            ),
            price = MealPriceDto(
                name = "Vegaanilounas",
                value = PriceValueDto(student = "3.10", normal = "9.50")
            )
        )

        val meal = UniCafeMapper.mapToMeal(mealDto, "test_1")
        assertEquals("Linssikeitto", meal.name)
        assertEquals("Vegaanilounas", meal.category)
        assertEquals("€3.10", meal.studentPrice)
        assertEquals("€9.50", meal.normalPrice)
        assertEquals(listOf("Veg", "G", "M"), meal.dietaryBadges)
        assertEquals(listOf("Selleri"), meal.allergens)
        assertEquals("Hiilijalanjälki: 0.28 kg CO2e / annos", meal.carbonFootprint)
    }

    @Test
    fun `test NutritionInfo parse extracts macros correctly from UniCafe string`() {
        val raw = "Energia 801kJ, energia 191kcal, rasva 11g, josta tyydyttynyttä rasvaa 0,8g, hiilihydraatit 9,6g, josta sokereita 2,2g, proteiini 13g, suola 0,98g, ravintokuitua 3,6g, laktoosia 0g."
        val info = fi.pushan.unicafedaily.domain.model.NutritionInfo.parse(raw)
        assertNotNull(info)
        assertEquals("191 kcal", info?.caloriesKcal)
        assertEquals("801 kJ", info?.energyKj)
        assertEquals("11g", info?.fat)
        assertEquals("0.8g", info?.saturatedFat)
        assertEquals("9.6g", info?.carbs)
        assertEquals("2.2g", info?.sugars)
        assertEquals("13g", info?.protein)
        assertEquals("0.98g", info?.salt)
        assertEquals("3.6g", info?.fiber)
        assertEquals("0g", info?.lactose)
    }

    @Test
    fun `test MealType detection identifies food categories`() {
        assertEquals(
            fi.pushan.unicafedaily.domain.model.MealType.SOUP,
            fi.pushan.unicafedaily.domain.model.MealType.detect("Linssikeitto", "Lounas", listOf("Veg"))
        )
        assertEquals(
            fi.pushan.unicafedaily.domain.model.MealType.FISH,
            fi.pushan.unicafedaily.domain.model.MealType.detect("Paistettua lohta ja tilliperunoita", "Kala", emptyList())
        )
        assertEquals(
            fi.pushan.unicafedaily.domain.model.MealType.CHICKEN,
            fi.pushan.unicafedaily.domain.model.MealType.detect("Broilericurry ja riisi", "Lounas", emptyList())
        )
        assertEquals(
            fi.pushan.unicafedaily.domain.model.MealType.VEGAN,
            fi.pushan.unicafedaily.domain.model.MealType.detect("Tofuwokki", "Vegaani", listOf("Veg"))
        )
    }
}

