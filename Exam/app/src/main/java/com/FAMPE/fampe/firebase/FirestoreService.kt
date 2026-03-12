package com.FAMPE.fampe.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {

    val db = FirebaseFirestore.getInstance()

    fun updatePlayerLocation(userId: String, lat: Double, lng: Double) {

        val playerData = mapOf(
            "lat" to lat,
            "lng" to lng,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("players")
            .document(userId)
            .set(playerData)
    }
}