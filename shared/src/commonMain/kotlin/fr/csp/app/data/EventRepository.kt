package fr.csp.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class EventRepository {
    private val db = Firebase.firestore

    suspend fun createEvent(
        title: String,
        type: String,
        date: String,
        time: String,
        location: String,
    ) {
        db.collection("events").add(
            mapOf(
                "title" to title,
                "type" to type,
                "date" to date,
                "time" to time,
                "location" to location,
                "status" to "OPEN",
                "participants_count" to 0,
                "featured" to false,
            )
        )
    }
}
