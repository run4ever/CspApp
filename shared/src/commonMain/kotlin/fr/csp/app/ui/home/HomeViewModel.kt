package fr.csp.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.data.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class UserDoc(
    val prenom: String,
    val nom: String,
    val dateNaissance: String,
    val role: String,
    val status: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel : ViewModel() {
    private val repository = EventRepository()

    val events = repository.getEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val currentUid: Flow<String?> = Firebase.auth.authStateChanged
        .map { it?.uid }

    val userDoc: StateFlow<UserDoc?> = currentUid.flatMapLatest { uid ->
        if (uid == null) flowOf(null)
        else Firebase.firestore.collection("users").document(uid).snapshots
            .map { doc ->
                if (!doc.exists) null
                else UserDoc(
                    prenom = doc.get<String?>("prenom") ?: "",
                    nom = doc.get<String?>("nom") ?: "",
                    dateNaissance = doc.get<String?>("date_naissance") ?: "",
                    role = doc.get<String?>("role") ?: "",
                    status = doc.get<String?>("status") ?: "",
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isAdmin: StateFlow<Boolean> = userDoc
        .map { it?.role == "admin" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val pendingCount: StateFlow<Int> = isAdmin.flatMapLatest { admin ->
        if (!admin) flowOf(0)
        else Firebase.firestore.collection("users")
            .where { "status" equalTo "PENDING" }
            .snapshots
            .map { it.documents.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
