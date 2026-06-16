package fr.csp.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FavoriteLocation(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String = "",
)

class FavoriteRepository {
    private val favoritesRef = Firebase.firestore.collection("club_favorite_addresses")

    fun getFavorites(): Flow<List<FavoriteLocation>> =
        favoritesRef.snapshots.map { snap ->
            snap.documents.mapNotNull { doc ->
                try {
                    FavoriteLocation(
                        id = doc.id,
                        name = doc.get<String?>("name") ?: return@mapNotNull null,
                        lat = doc.get<Double?>("lat") ?: return@mapNotNull null,
                        lon = doc.get<Double?>("lon") ?: return@mapNotNull null,
                        address = doc.get<String?>("address") ?: "",
                    )
                } catch (_: Exception) { null }
            }.sortedBy { it.name }
        }

    suspend fun addFavorite(name: String, address: String, lat: Double, lon: Double) {
        favoritesRef.add(mapOf("name" to name, "address" to address, "lat" to lat, "lon" to lon))
    }

    suspend fun deleteFavorite(id: String) {
        favoritesRef.document(id).delete()
    }
}
