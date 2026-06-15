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
)

enum class EventStatus { OPEN, CANCELLED, FULL }

val sampleEvents = listOf(
    ClubEvent(
        id = "rallye-bsd",
        title = "Rallye US Bois Saint-Denis",
        wd3 = "Mer.", day = 17, monthShort = "juin",
        weekday = "mer.", month = "juin", time = "8h00",
        status = EventStatus.OPEN, featured = true, participants = 36,
    ),
    ClubEvent(
        id = "equip",
        title = "Présentation des équipements 2026",
        wd3 = "Dim.", day = 21, monthShort = "juin",
        weekday = "dim.", month = "juin", time = "19h00",
        status = EventStatus.OPEN, participants = 42,
    ),
    ClubEvent(
        id = "sortie",
        title = "Sortie CSP",
        wd3 = "Sam.", day = 27, monthShort = "juin",
        weekday = "sam.", month = "juin", time = "8h00",
        status = EventStatus.CANCELLED, participants = 1,
    ),
    ClubEvent(
        id = "2x100km",
        title = "2 x 100 km",
        wd3 = "Dim.", day = 27, monthShort = "juin",
        weekday = "dim.", month = "juin", time = "7h30",
        status = EventStatus.OPEN, participants = 58,
    ),
)
