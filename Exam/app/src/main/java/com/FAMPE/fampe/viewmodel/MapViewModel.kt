package com.FAMPE.fampe.viewmodel

import androidx.lifecycle.ViewModel
import com.FAMPE.fampe.model.Player
import com.FAMPE.fampe.repository.PlayerRepository
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel  : ViewModel() {
    private val repository = PlayerRepository()

    fun updateLocation(userId: String, lat: Double, lng: Double) {
        repository.updateLocation(userId, lat, lng)
    }

    fun listenToPlayers(onUpdate: (List<Player>) -> Unit) {

        FirebaseFirestore.getInstance()
            .collection("players")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val players = snapshot.documents.mapNotNull { doc ->

                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null
                    val name = doc.getString("name") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0

                    Player(
                        id = doc.id,
                        location = location,
                        name = name,
                        score = score
                    )
                }

                onUpdate(players)
            }

    }
}