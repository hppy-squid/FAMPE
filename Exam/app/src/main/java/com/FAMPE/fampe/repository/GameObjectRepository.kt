package com.FAMPE.fampe.repository

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class GameObjectRepository {

    private val db = FirebaseFirestore.getInstance()

    private val radius = 0.2 // DIN radius (lat/lng offset)

    fun ensureObjectsNearPlayer(
        userLat: Double,
        userLng: Double,
        sessionId: String,
        targetCount: Int = 15
    ) {
        db.collection("objects")
            .whereEqualTo("active", true)
            .whereEqualTo("sessionId", sessionId)
            .get()
            .addOnSuccessListener { snapshot ->

                val nearbyObjects = snapshot.documents.count { doc ->
                    val location = doc.getGeoPoint("location") ?: return@count false

                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        userLat,
                        userLng,
                        location.latitude,
                        location.longitude,
                        distance
                    )

                    distance[0] <= 10000
                }

                val missing = targetCount - nearbyObjects

                if (missing > 0) {
                    repeat(missing) {
                        spawnObjectNearPlayer(userLat, userLng, sessionId)
                    }
                }
            }
    }

    fun spawnObjectNearPlayer(
        userLat: Double,
        userLng: Double,
        sessionId: String
    ) {
        val randomLat = userLat + (Math.random() - 0.5) * radius
        val randomLng = userLng + (Math.random() - 0.5) * radius

        val obj = mapOf(
            "active" to true,
            "foundBy" to "",
            "location" to GeoPoint(randomLat, randomLng),
            "points" to 10,
            "sessionId" to sessionId
        )

        db.collection("objects").add(obj)
    }
}