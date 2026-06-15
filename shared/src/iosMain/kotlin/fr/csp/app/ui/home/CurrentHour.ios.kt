package fr.csp.app.ui.home

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSDate

actual fun currentHour(): Int =
    NSCalendar.currentCalendar.components(NSCalendarUnitHour, NSDate()).hour.toInt()
