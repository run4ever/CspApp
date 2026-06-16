package fr.csp.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

data class FirestoreComment(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Long = 0L,
)

class CommentRepository {
    private fun ref(eventId: String) =
        Firebase.firestore.collection("events").document(eventId).collection("comments")

    fun getComments(eventId: String): Flow<List<FirestoreComment>> =
        ref(eventId).snapshots.map { snap ->
            snap.documents.mapNotNull { doc ->
                try {
                    FirestoreComment(
                        id = doc.id,
                        text = doc.get<String?>("text") ?: return@mapNotNull null,
                        authorId = doc.get<String?>("authorId") ?: "",
                        authorName = doc.get<String?>("authorName") ?: "",
                        createdAt = doc.get<Long?>("createdAt") ?: 0L,
                    )
                } catch (_: Exception) { null }
            }.sortedByDescending { it.createdAt }
        }

    suspend fun deleteComment(eventId: String, commentId: String) {
        ref(eventId).document(commentId).delete()
    }

    suspend fun addComment(eventId: String, text: String, authorId: String, authorName: String) {
        ref(eventId).add(mapOf(
            "text" to text,
            "authorId" to authorId,
            "authorName" to authorName,
            "createdAt" to Clock.System.now().toEpochMilliseconds(),
        ))
    }
}
