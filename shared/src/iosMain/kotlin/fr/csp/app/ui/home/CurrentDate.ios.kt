package fr.csp.app.ui.home

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentDateIso(): String {
    val cal = NSCalendar.currentCalendar
    val c = cal.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, NSDate())
    val y = c.year.toString().padStart(4, '0')
    val m = c.month.toString().padStart(2, '0')
    val d = c.day.toString().padStart(2, '0')
    return "$y-$m-$d"
}
