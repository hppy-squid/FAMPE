package com.FAMPE.fampe.repository

import android.location.Location
import com.FAMPE.fampe.firebase.FirestoreService
import com.FAMPE.fampe.model.GameObject
import com.google.firebase.firestore.FirebaseFirestore

class PlayerRepository {

    fun updateLocation(userId: String, lat: Double, lng: Double) {
        FirestoreService.updatePlayerLocation(userId, lat, lng)
    }

    fun getNearbyObjects(
        lat: Double,
        lng: Double,
        sessionId: String = "currentSession",
        radiusMeters: Float = 100f,
        callback: (List<GameObject>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("objects")
            .whereEqualTo("active", true)
            .whereEqualTo("sessionId", sessionId)
            .get()
            .addOnSuccessListener { snapshot ->

                val nearby = snapshot.documents.mapNotNull { doc ->
                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null

                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        lat, lng,
                        location.latitude, location.longitude,
                        distance
                    )

                    if (distance[0] > radiusMeters) return@mapNotNull null

                    GameObject(
                        id = doc.id,
                        sessionId = doc.getString("sessionId") ?: "",
                        active = doc.getBoolean("active") ?: true,
                        foundBy = doc.getString("foundBy") ?: "",
                        location = location,
                        points = doc.getLong("points")?.toInt() ?: 0
                    )
                }

                callback(nearby)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}