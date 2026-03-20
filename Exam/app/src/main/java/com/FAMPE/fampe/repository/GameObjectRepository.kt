package com.FAMPE.fampe.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class GameObjectRepository {

    fun spawnObjectNearPlayer(
        userLat: Double,
        userLng: Double,
        sessionId: String
    ) {

        val db = FirebaseFirestore.getInstance()

        val radius = 0.001 // ~100m

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