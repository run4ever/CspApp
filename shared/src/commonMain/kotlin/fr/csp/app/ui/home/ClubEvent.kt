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
    val participantIds: List<String> = emptyList(),
    val dateSort: String = "",
    val location: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val description: String = "",
    val type: String = "",
    val seriesId: String? = null,
    val traceIds: List<String> = emptyList(),
)

enum class EventStatus { OPEN, CANCELLED, FULL }
