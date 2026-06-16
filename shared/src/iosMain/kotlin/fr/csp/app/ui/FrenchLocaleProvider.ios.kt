package fr.csp.app.ui

import androidx.compose.material3.CalendarLocale
import platform.Foundation.NSLocale

actual fun frenchCalendarLocale(): CalendarLocale = NSLocale("fr_FR")
