package com.FAMPE.fampe.service

import android.util.Log
import android.location.Location
import com.FAMPE.fampe.model.GameObject
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GameObjectService {


    fun tryPickupObject(
        playerLat: Double,
        playerLng: Double,
        gameObject: GameObject,
        userId: String
    ) {

        val distance = FloatArray(1)

        Location.distanceBetween(
            playerLat, playerLng,
            gameObject.location.latitude,
            gameObject.location.longitude,
            distance
        )

        if (distance[0] >= 20 || !gameObject.active || gameObject.id.isBlank()) return

        val db = FirebaseFirestore.getInstance()
        val objectRef = db.collection("objects").document(gameObject.id)
        val playerRef = db.collection("players").document(userId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(objectRef)
            val isActive = snapshot.getBoolean("active") ?: false

            if (!isActive) return@runTransaction false

            transaction.update(objectRef, mapOf(
                "active" to false,
                "foundBy" to userId

            ))

            transaction.update(playerRef,
                "score",
                FieldValue.increment(gameObject.points.toLong())

            )
            Log.d("GameObjectService", "Object picked up by $userId")


            true
        }
    }
}