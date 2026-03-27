package com.FAMPE.fampe.firebase

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions

object FirestoreService {

    val db = FirebaseFirestore.getInstance()

    fun updatePlayerLocation(userId: String, lat: Double, lng: Double) {

        val location = GeoPoint(lat, lng)

        val playerData = mapOf(
            "location" to location,
            "lastUpdated" to FieldValue.serverTimestamp()
        )

        db.collection("players")
            .document(userId)
            .set(playerData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "Location updated successfully for $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to update location for $userId", e)
            }
    }
}