package fr.csp.app.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.*

class EventDetailViewModel(private val eventId: String) : ViewModel() {

    private val eventRef = Firebase.firestore.collection("events").document(eventId)

    val currentUid: StateFlow<String?> = Firebase.auth.authStateChanged
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val participants: StateFlow<List<String>> = eventRef.snapshots
        .map { doc -> doc.get<List<String>?>("participants") ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isLoggedIn: StateFlow<Boolean?> = currentUid
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isJoined: StateFlow<Boolean> = combine(currentUid, participants) { uid, parts ->
        uid != null && uid in parts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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

    suspend fun toggleParticipation(): String? = try {
        val uid = Firebase.auth.currentUser?.uid ?: return "Non connecté"
        val current = participants.value
        val updated = if (uid in current) current - uid else current + uid
        eventRef.set(mapOf("participants" to updated), merge = true)
        null
    } catch (e: Exception) {
        e.message ?: "Erreur"
    }
}
