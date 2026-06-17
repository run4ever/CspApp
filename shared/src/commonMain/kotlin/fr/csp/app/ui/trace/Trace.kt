package fr.csp.app.ui.trace

data class Trace(
    val id: String,
    val title: String,
    val distanceKm: Double,
    val elevationUp: Int,
    val elevationDown: Int,
    val durationMin: Int,
    val url: String,
    val order: Int = 0,
)

fun Double.formatKm(): String {
    val tenths = (this * 10).toLong()
    return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
}

fun Int.toHhMm(): String {
    val h = this / 60
    val m = this % 60
    return if (h > 0) "${h}h${m.toString().padStart(2, '0')}" else "${m}min"
}
