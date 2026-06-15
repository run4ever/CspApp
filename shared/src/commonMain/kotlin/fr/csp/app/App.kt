package fr.csp.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.csp.app.data.DatabaseDriverFactory
import fr.csp.app.ui.event.EventDetailScreen
import fr.csp.app.ui.home.ClubEvent
import fr.csp.app.ui.home.HomeScreen

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    var selectedEvent by remember { mutableStateOf<ClubEvent?>(null) }

    if (selectedEvent != null) {
        EventDetailScreen(event = selectedEvent!!, onBack = { selectedEvent = null })
    } else {
        HomeScreen(onEventClick = { event -> selectedEvent = event })
    }
}
