package fr.csp.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.ui.trace.Trace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TraceRepository {
    private val db = Firebase.firestore

    fun getTraces(): Flow<List<Trace>> =
        db.collection("traces").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { it.toTrace() }.sortedBy { it.order }
        }

    suspend fun addTrace(
        title: String,
        distanceKm: Double,
        elevationUp: Int,
        elevationDown: Int,
        durationMin: Int,
        url: String,
        order: Int = 0,
    ) {
        db.collection("traces").add(
            mapOf(
                "title" to title,
                "distanceKm" to distanceKm,
                "elevationUp" to elevationUp,
                "elevationDown" to elevationDown,
                "durationMin" to durationMin,
                "url" to url,
                "order" to order,
            )
        )
    }

    suspend fun deleteTrace(id: String) {
        db.collection("traces").document(id).delete()
    }
}

private fun DocumentSnapshot.toTrace(): Trace? = try {
    Trace(
        id = id,
        title = get<String?>("title") ?: return null,
        distanceKm = get<Double?>("distanceKm") ?: 0.0,
        elevationUp = get<Long?>("elevationUp")?.toInt() ?: 0,
        elevationDown = get<Long?>("elevationDown")?.toInt() ?: 0,
        durationMin = get<Long?>("durationMin")?.toInt() ?: 0,
        url = get<String?>("url") ?: "",
        order = get<Long?>("order")?.toInt() ?: 0,
    )
} catch (_: Exception) { null }
