package com.FAMPE.fampe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.FAMPE.fampe.model.GameObject
import com.FAMPE.fampe.model.Player
import com.FAMPE.fampe.repository.GameObjectRepository
import com.FAMPE.fampe.repository.PlayerRepository
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel  : ViewModel() {
    private val repository = PlayerRepository()
    private val objRepository = GameObjectRepository()


    fun updateLocation(userId: String, lat: Double, lng: Double) {
        repository.updateLocation(userId, lat, lng)

        objRepository.ensureObjectsNearPlayer(
            lat,
            lng,
            "currentSession"
        )
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


                fun onPlayerMoved(lat: Double, lng: Double) {

                    // hämta antal objekt nära spelaren först
                    repository.getNearbyObjects(lat, lng) { objects ->

                        if (objects.size < 5) {
                            objRepository.spawnObjectNearPlayer(lat, lng, "currentSession")
                        }
                    }
                }


            }

    }

    fun listenToObjects(onUpdate: (List<GameObject>) -> Unit) {

        FirebaseFirestore.getInstance()
            .collection("objects")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val objects = snapshot.documents.mapNotNull { doc ->

                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null

                    GameObject(
                        sessionId = doc.id,
                        location = location,
                        active = doc.getBoolean("active") ?: true,
                        points = doc.getLong("points")?.toInt() ?: 0
                    )
                }

                onUpdate(objects)
            }
    }
}
