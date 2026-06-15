package fr.csp.app.ui.home

data class ClubEvent(
    val id: String,
    val title: String,
    val wd3: String,
    val day: Int,
    val monthShort: String,
    val weekday: String,
    val month: String,
    val time: String,
    val status: EventStatus,
    val featured: Boolean = false,
    val participants: Int = 0,
    val dateSort: String = "",
)

enum class EventStatus { OPEN, CANCELLED, FULL }
