package com.FAMPE.fampe.repository

import com.FAMPE.fampe.firebase.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore

class PlayerRepository {
    fun updateLocation(userId: String, lat: Double, lng: Double) {
        FirestoreService.updatePlayerLocation(userId, lat, lng)
    }

    fun getNearbyObjects(lat: Double, lng: Double, callback: (List<Object>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("objects")
            .whereEqualTo("active", true)
            .whereEqualTo("sessionId", "currentSession")
            .get()
    }

}