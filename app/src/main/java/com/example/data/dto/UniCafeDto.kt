package com.example.data.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class RestaurantDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "permalink") val permalink: String? = null,
    @Json(name = "location") val location: List<LocationDto>? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "menuData") val menuData: MenuDataDto? = null,
    @Json(name = "areacode") val areacode: Int? = null
)

@JsonClass(generateAdapter = true)
data class LocationDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class MenuDataDto(
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "feedback_address") val feedbackAddress: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "visitingHours") val visitingHours: VisitingHoursDto? = null,
    @Json(name = "menus") val menus: List<DailyMenuDto>? = null
)

@JsonClass(generateAdapter = true)
data class VisitingHoursDto(
    @Json(name = "business") val business: VisitingHoursSectionDto? = null,
    @Json(name = "lounas") val lounas: VisitingHoursSectionDto? = null
)

@JsonClass(generateAdapter = true)
data class VisitingHoursSectionDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "items") val items: List<VisitingHoursItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class VisitingHoursItemDto(
    @Json(name = "label") val label: String? = null,
    @Json(name = "hours") val hours: String? = null,
    @Json(name = "closedException") val closedException: Boolean? = false,
    @Json(name = "exception") val exception: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class DailyMenuDto(
    @Json(name = "date") val date: String,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: List<MealDto>? = null
)

@JsonClass(generateAdapter = true)
data class MealDto(
    @Json(name = "name") val name: String,
    @Json(name = "ingredients") val ingredients: String? = null,
    @Json(name = "nutrition") val nutrition: String? = null,
    @Json(name = "meta") val meta: Map<String, List<String>>? = null,
    @Json(name = "price") val price: MealPriceDto? = null
)

@JsonClass(generateAdapter = true)
data class MealPriceDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: PriceValueDto? = null
)

data class PriceValueDto(
    val student: String? = null,
    val graduate: String? = null,
    val contract: String? = null,
    val normal: String? = null,
    val studentHyy: String? = null,
    val graduateHyy: String? = null
)

// --- Custom Moshi Adapters for PHP JSON Quirks ---

class PriceValueAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): PriceValueDto? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                var student: String? = null
                var graduate: String? = null
                var contract: String? = null
                var normal: String? = null
                var studentHyy: String? = null
                var graduateHyy: String? = null

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "student" -> student = reader.nextString()
                        "graduate" -> graduate = reader.nextString()
                        "contract" -> contract = reader.nextString()
                        "normal" -> normal = reader.nextString()
                        "student_hyy" -> studentHyy = reader.nextString()
                        "graduate_hyy" -> graduateHyy = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                PriceValueDto(student, graduate, contract, normal, studentHyy, graduateHyy)
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                // In PHP json_encode, an empty associative array [] is output instead of {}
                reader.skipValue()
                null
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: PriceValueDto?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        value.student?.let { writer.name("student").value(it) }
        value.graduate?.let { writer.name("graduate").value(it) }
        value.contract?.let { writer.name("contract").value(it) }
        value.normal?.let { writer.name("normal").value(it) }
        value.studentHyy?.let { writer.name("student_hyy").value(it) }
        value.graduateHyy?.let { writer.name("graduate_hyy").value(it) }
        writer.endObject()
    }
}

class VisitingHoursAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, delegate: com.squareup.moshi.JsonAdapter<VisitingHoursDto>): VisitingHoursDto? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)
            JsonReader.Token.STRING -> {
                reader.nextString()
                null
            }
            JsonReader.Token.BOOLEAN -> {
                reader.nextBoolean()
                null
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: VisitingHoursDto?, delegate: com.squareup.moshi.JsonAdapter<VisitingHoursDto>) {
        delegate.toJson(writer, value)
    }
}

class VisitingHoursSectionAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, delegate: com.squareup.moshi.JsonAdapter<VisitingHoursSectionDto>): VisitingHoursSectionDto? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)
            JsonReader.Token.BOOLEAN -> {
                reader.nextBoolean()
                null
            }
            JsonReader.Token.STRING -> {
                reader.nextString()
                null
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: VisitingHoursSectionDto?, delegate: com.squareup.moshi.JsonAdapter<VisitingHoursSectionDto>) {
        delegate.toJson(writer, value)
    }
}
