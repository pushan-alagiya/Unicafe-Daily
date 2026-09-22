package com.example.domain.model

enum class CustomerCategory(
    val id: String,
    val displayName: String,
    val shortLabel: String
) {
    STUDENT("student", "Student", "Student"),
    GRADUATE("graduate", "Post-graduate", "Post-grad"),
    STAFF("staff", "Staff & pensioner", "Staff"),
    NORMAL("normal", "Normal price", "Normal");

    companion object {
        fun fromId(id: String?): CustomerCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: STUDENT
        }
    }
}
