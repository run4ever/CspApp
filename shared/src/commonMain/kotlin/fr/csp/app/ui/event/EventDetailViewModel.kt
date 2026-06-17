package fr.csp.app.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.data.EventRepository
import fr.csp.app.ui.trace.Trace
import kotlinx.coroutines.flow.*

class EventDetailViewModel(private val eventId: String) : ViewModel() {

    private val eventRef = Firebase.firestore.collection("events").document(eventId)
    private val repository = EventRepository()

    val currentUid: StateFlow<String?> = Firebase.auth.authStateChanged
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val participants: StateFlow<List<String>> = eventRef.snapshots
        .map { doc -> doc.get<List<String>?>("participants") ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // { uid → "G1"/"G2"/"G3" } — uniquement renseigné pour les Sorties hebdo CSP
    val participantGroups: StateFlow<Map<String, String>> = eventRef.snapshots
        .map { doc ->
            try { doc.get<Map<String, String>?>("participantGroups") ?: emptyMap() }
            catch (_: Exception) { emptyMap() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val isLoggedIn: StateFlow<Boolean?> = currentUid
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isJoined: StateFlow<Boolean> = combine(currentUid, participants) { uid, parts ->
        uid != null && uid in parts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val myGroup: StateFlow<String?> = combine(currentUid, participantGroups) { uid, groups ->
        if (uid != null) groups[uid]?.takeIf { it.isNotEmpty() } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val traces: StateFlow<List<Trace>> = eventRef.snapshots
        .flatMapLatest { doc ->
            val ids = try { doc.get<List<String>?>("traceIds") ?: emptyList() } catch (_: Exception) { emptyList() }
            if (ids.isEmpty()) flowOf(emptyList())
            else flow {
                emit(ids.mapNotNull { id ->
                    try {
                        val snap = Firebase.firestore.collection("traces").document(id).get()
                        Trace(
                            id = snap.id,
                            title = snap.get<String?>("title") ?: return@mapNotNull null,
                            distanceKm = snap.get<Double?>("distanceKm") ?: 0.0,
                            elevationUp = snap.get<Long?>("elevationUp")?.toInt() ?: 0,
                            elevationDown = snap.get<Long?>("elevationDown")?.toInt() ?: 0,
                            durationMin = snap.get<Long?>("durationMin")?.toInt() ?: 0,
                            url = snap.get<String?>("url") ?: "",
                        )
                    } catch (_: Exception) { null }
                })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val participantPhotos: StateFlow<Map<String, String?>> = participants
        .flatMapLatest { uids ->
            if (uids.isEmpty()) flowOf(emptyMap())
            else flow {
                emit(uids.associate { uid ->
                    uid to try {
                        Firebase.firestore.collection("users").document(uid).get()
                            .get<String?>("photoUrl")
                    } catch (_: Exception) { null }
                })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Rejoindre l'événement avec un groupe (group = "" pour les événements non-hebdo)
    suspend fun joinWithGroup(group: String): String? = try {
        val uid = Firebase.auth.currentUser?.uid ?: return "Non connecté"
        val updated = if (uid in participants.value) participants.value else participants.value + uid
        val updatedGroups = participantGroups.value.toMutableMap().also {
            if (group.isNotEmpty()) it[uid] = group else it.remove(uid)
        }
        eventRef.set(
            mapOf("participants" to updated, "participantGroups" to updatedGroups),
            merge = true,
        )
        null
    } catch (e: Exception) {
        e.message ?: "Erreur"
    }

    suspend fun deleteEvent(): String? = try {
        eventRef.delete()
        null
    } catch (e: Exception) {
        e.message ?: "Erreur"
    }

    suspend fun deleteSeriesFrom(seriesId: String, fromDateSort: String): String? = try {
        repository.deleteEventSeriesFrom(seriesId, fromDateSort)
        null
    } catch (e: Exception) {
        e.message ?: "Erreur"
    }

    suspend fun leaveEvent(): String? = try {
        val uid = Firebase.auth.currentUser?.uid ?: return "Non connecté"
        val updated = participants.value - uid
        val updatedGroups = participantGroups.value.toMutableMap().also { it.remove(uid) }
        eventRef.set(
            mapOf("participants" to updated, "participantGroups" to updatedGroups),
            merge = true,
        )
        null
    } catch (e: Exception) {
        e.message ?: "Erreur"
    }
}
