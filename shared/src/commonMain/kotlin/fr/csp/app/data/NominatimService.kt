package fr.csp.app.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class LocationSuggestion(
    val displayName: String,
    val lat: Double,
    val lon: Double,
)

@Serializable
private data class NominatimResult(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String,
)

object NominatimService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun reverse(lat: Double, lon: Double): LocationSuggestion? = try {
        val r = client.get("https://nominatim.openstreetmap.org/reverse") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("format", "json")
            parameter("accept-language", "fr")
            header("User-Agent", "CspApp/1.0 (fr.csp.app)")
        }.body<NominatimResult>()
        val rlat = r.lat.toDoubleOrNull() ?: return null
        val rlon = r.lon.toDoubleOrNull() ?: return null
        LocationSuggestion(r.displayName, rlat, rlon)
    } catch (_: Exception) { null }

    suspend fun search(query: String): List<LocationSuggestion> {
        if (query.length < 3) return emptyList()
        return client.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", "5")
            parameter("accept-language", "fr")
            header("User-Agent", "CspApp/1.0 (fr.csp.app)")
        }.body<List<NominatimResult>>().mapNotNull { r ->
            val lat = r.lat.toDoubleOrNull() ?: return@mapNotNull null
            val lon = r.lon.toDoubleOrNull() ?: return@mapNotNull null
            LocationSuggestion(r.displayName, lat, lon)
        }
    }
}
