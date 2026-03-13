package com.FAMPE.fampe.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

object FirestoreService {

    val db = FirebaseFirestore.getInstance()

    fun updatePlayerLocation(userId: String, lat: Double, lng: Double) {

        val location = GeoPoint(lat, lng)

        val playerData = mapOf(
            "location" to location,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("players")
            .document(userId)
            .set(playerData)
    }
}