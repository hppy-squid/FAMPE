package com.FAMPE.fampe.service

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

        if (distance[0] < 20 && gameObject.active) {

            FirebaseFirestore.getInstance()
                .collection("objects")
                .document(gameObject.sessionId)
                .update(
                    mapOf(
                        "active" to false,
                        "foundBy" to userId
                    )
                )

            // uppdatera score
            FirebaseFirestore.getInstance()
                .collection("players")
                .document(userId)
                .update("score", FieldValue.increment(gameObject.points.toLong()))
        }
    }
}