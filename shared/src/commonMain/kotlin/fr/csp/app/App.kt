package fr.csp.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.csp.app.data.DatabaseDriverFactory
import fr.csp.app.ui.event.EventDetailScreen
import fr.csp.app.ui.home.ClubEvent
import fr.csp.app.ui.home.HomeScreen
import fr.csp.app.ui.home.HomeViewModel
import fr.csp.app.ui.profile.EditEventScreen
import fr.csp.app.ui.profile.ProfileScreen

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val vm = viewModel { HomeViewModel() }
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val userDoc by vm.userDoc.collectAsStateWithLifecycle()

    var selectedEvent by remember { mutableStateOf<ClubEvent?>(null) }
    var editingEvent by remember { mutableStateOf<ClubEvent?>(null) }
    var showCreateEvent by remember { mutableStateOf(false) }
    var homeTab by remember { mutableStateOf(0) }
    var menuStartOnAuth by remember { mutableStateOf(false) }

    // Toujours utiliser la version live de l'événement (mise à jour Firestore en temps réel)
    val liveEvent = remember(selectedEvent?.id, events) {
        selectedEvent?.let { sel -> events.find { it.id == sel.id } ?: sel }
    }
    val currentIndex = remember(selectedEvent?.id, events) {
        events.indexOfFirst { it.id == selectedEvent?.id }
    }
    val prevEvent = remember(currentIndex, events) { if (currentIndex > 0) events[currentIndex - 1] else null }
    val nextEvent = remember(currentIndex, events) { if (currentIndex < events.lastIndex) events[currentIndex + 1] else null }

    when {
        showCreateEvent -> ProfileScreen(onBack = { showCreateEvent = false; homeTab = 0 })
        editingEvent != null -> EditEventScreen(
            event = editingEvent!!,
            onDone = { editingEvent = null },
        )
        liveEvent != null -> EventDetailScreen(
            event = liveEvent,
            onBack = { selectedEvent = null },
            isAdmin = isAdmin,
            onEdit = { editingEvent = liveEvent },
            onDelete = { selectedEvent = null; homeTab = 0 },
            onPrevious = prevEvent?.let { prev -> { selectedEvent = prev } },
            onNext = nextEvent?.let { next -> { selectedEvent = next } },
            userCanComment = userDoc?.canComment ?: true,
            userPhotoUrl = userDoc?.photoUrl,
            onLogin = { selectedEvent = null; homeTab = 3; menuStartOnAuth = true },
        )
        else -> HomeScreen(
            onEventClick = { event -> selectedEvent = event },
            onCreateEvent = { showCreateEvent = true },
            selectedTab = homeTab,
            onTabSelected = { homeTab = it },
            menuStartOnAuth = menuStartOnAuth,
            onMenuAuthHandled = { menuStartOnAuth = false },
            onLoginRequest = { homeTab = 3; menuStartOnAuth = true },
        )
    }
}
