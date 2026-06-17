package fr.csp.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.ui.home.ClubEvent
import fr.csp.app.ui.home.EventStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class EventRepository {
    private val db = Firebase.firestore

    fun getEvents(): Flow<List<ClubEvent>> =
        db.collection("events").snapshots.map { snapshot ->
            snapshot.documents
                .mapNotNull { it.toClubEvent() }
                .sortedBy { it.dateSort }
        }

    suspend fun updateEvent(
        id: String,
        title: String,
        type: String,
        date: String,
        time: String,
        location: String,
        lat: Double? = null,
        lon: Double? = null,
        description: String = "",
        traceIds: List<String> = emptyList(),
        status: String = "OPEN",
    ) {
        db.collection("events").document(id).set(
            buildMap<String, Any?> {
                put("title", title)
                put("type", type)
                put("date", date)
                put("time", time)
                put("location", location)
                put("description", description)
                put("lat", lat)
                put("lon", lon)
                put("traceIds", traceIds)
                put("status", status)
            },
            merge = true,
        )
    }

    suspend fun createEvent(
        title: String,
        type: String,
        date: String,
        time: String,
        location: String,
        lat: Double? = null,
        lon: Double? = null,
        description: String = "",
        repeatUntil: String? = null,
        traceIds: List<String> = emptyList(),
    ) {
        val dates = buildRecurrenceDates(date, repeatUntil)
        val seriesId = if (dates.size > 1) generateSeriesId() else null
        dates.forEach { d ->
            db.collection("events").add(
                buildMap {
                    put("title", title)
                    put("type", type)
                    put("date", d)
                    put("time", time)
                    put("location", location)
                    put("status", "OPEN")
                    put("participants_count", 0)
                    put("featured", false)
                    if (lat != null) put("lat", lat)
                    if (lon != null) put("lon", lon)
                    put("description", description)
                    if (seriesId != null) put("seriesId", seriesId)
                    put("traceIds", traceIds)
                }
            )
        }
    }

    suspend fun updateEventSeries(
        seriesId: String,
        fromDateSort: String,
        title: String,
        type: String,
        time: String,
        location: String,
        lat: Double? = null,
        lon: Double? = null,
        description: String = "",
    ) {
        db.collection("events")
            .where { "seriesId" equalTo seriesId }
            .get().documents
            .filter { doc ->
                val dateStr = try { doc.get<String?>("date") } catch (_: Exception) { null } ?: ""
                (parseDateFr(dateStr)?.toString() ?: dateStr) >= fromDateSort
            }
            .forEach { doc ->
                db.collection("events").document(doc.id).set(
                    buildMap<String, Any?> {
                        put("title", title)
                        put("type", type)
                        put("time", time)
                        put("location", location)
                        put("description", description)
                        put("lat", lat)
                        put("lon", lon)
                    },
                    merge = true,
                )
            }
    }

    suspend fun deleteEventSeriesFrom(seriesId: String, fromDateSort: String) {
        db.collection("events")
            .where { "seriesId" equalTo seriesId }
            .get().documents
            .filter { doc ->
                val dateStr = try { doc.get<String?>("date") } catch (_: Exception) { null } ?: ""
                (parseDateFr(dateStr)?.toString() ?: dateStr) >= fromDateSort
            }
            .forEach { doc -> db.collection("events").document(doc.id).delete() }
    }
}

private fun generateSeriesId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..20).map { chars.random() }.joinToString("")
}

private fun buildRecurrenceDates(startDate: String, repeatUntil: String?): List<String> {
    if (repeatUntil == null) return listOf(startDate)
    val start = parseDateFr(startDate) ?: return listOf(startDate)
    val end = parseDateFr(repeatUntil) ?: return listOf(startDate)
    val dates = mutableListOf<String>()
    var current = start
    while (current <= end) {
        dates.add(current.toDateFr())
        current = current.plus(1, DateTimeUnit.WEEK)
    }
    return dates.ifEmpty { listOf(startDate) }
}

private fun LocalDate.toDateFr(): String =
    "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"

private fun DocumentSnapshot.toClubEvent(): ClubEvent? {
    return try {
        val title = get<String?>("title") ?: return null
        val dateStr = get<String?>("date") ?: ""
        val timeStr = get<String?>("time") ?: ""
        val statusStr = get<String?>("status") ?: "OPEN"
        val featured = get<Boolean?>("featured") ?: false
        val participantsArray = get<List<String>?>("participants") ?: emptyList()
        val participants = if (participantsArray.isNotEmpty()) participantsArray.size
                          else get<Long?>("participants_count")?.toInt() ?: 0
        val localDate = parseDateFr(dateStr)
        val location = get<String?>("location") ?: ""
        val lat = get<Double?>("lat")
        val lon = get<Double?>("lon")
        val description = get<String?>("description") ?: ""
        val type = get<String?>("type") ?: ""
        val seriesId = get<String?>("seriesId")
        val traceIds = try { get<List<String>?>("traceIds") ?: emptyList() } catch (_: Exception) { emptyList() }
        ClubEvent(
            id = id,
            title = title,
            wd3 = localDate?.toWd3Fr() ?: "",
            day = localDate?.dayOfMonth ?: 0,
            monthShort = localDate?.toMonthShortFr() ?: "",
            weekday = localDate?.toWeekdayFr() ?: "",
            month = localDate?.toMonthLongFr() ?: "",
            time = timeStr,
            status = when (statusStr) {
                "CANCELLED" -> EventStatus.CANCELLED
                "FULL" -> EventStatus.FULL
                else -> EventStatus.OPEN
            },
            featured = featured,
            participants = participants,
            participantIds = participantsArray,
            dateSort = localDate?.toString() ?: dateStr,
            location = location,
            lat = lat,
            lon = lon,
            description = description,
            type = type,
            seriesId = seriesId,
            traceIds = traceIds,
        )
    } catch (_: Exception) {
        null
    }
}

private fun parseDateFr(dateStr: String): LocalDate? {
    return try {
        val (d, m, y) = dateStr.split("/")
        LocalDate(y.toInt(), m.toInt(), d.toInt())
    } catch (_: Exception) {
        null
    }
}

private fun LocalDate.toWd3Fr() = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Lun."
    DayOfWeek.TUESDAY -> "Mar."
    DayOfWeek.WEDNESDAY -> "Mer."
    DayOfWeek.THURSDAY -> "Jeu."
    DayOfWeek.FRIDAY -> "Ven."
    DayOfWeek.SATURDAY -> "Sam."
    DayOfWeek.SUNDAY -> "Dim."
    else -> ""
}

private fun LocalDate.toWeekdayFr() = toWd3Fr().lowercase()

private fun LocalDate.toMonthShortFr() = when (month.ordinal + 1) {
    1 -> "jan."; 2 -> "fév."; 3 -> "mars"; 4 -> "avr."
    5 -> "mai"; 6 -> "juin"; 7 -> "juil."; 8 -> "août"
    9 -> "sep."; 10 -> "oct."; 11 -> "nov."; else -> "déc."
}

private fun LocalDate.toMonthLongFr() = when (month.ordinal + 1) {
    1 -> "janvier"; 2 -> "février"; 3 -> "mars"; 4 -> "avril"
    5 -> "mai"; 6 -> "juin"; 7 -> "juillet"; 8 -> "août"
    9 -> "septembre"; 10 -> "octobre"; 11 -> "novembre"; else -> "décembre"
}
