package com.FAMPE.fampe.viewmodel

import android.util.Log
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
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("MapViewModel", "Error listening to players", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("MapViewModel", "Snapshot is null")
                    return@addSnapshotListener
                }

                val players = snapshot.documents.mapNotNull { doc ->

                    val location = doc.getGeoPoint("location") ?: run {
                        Log.w("MapViewModel", "No location for doc ${doc.id}")
                        return@mapNotNull null
                    }
                    val name = doc.getString("name") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val lastUpdated = doc.getTimestamp("lastUpdated")?.toDate()?.time ?: 0L

                    Player(
                        id = doc.id,
                        location = location,
                        lastUpdated = lastUpdated,
                        name = name,
                        score = score
                    )
                }

                Log.d("MapViewModel", "Players updated: ${players.size}")
                onUpdate(players)
            }

    }
}