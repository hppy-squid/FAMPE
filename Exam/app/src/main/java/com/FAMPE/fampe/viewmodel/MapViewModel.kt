package com.FAMPE.fampe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.FAMPE.fampe.model.GameObject
import com.FAMPE.fampe.model.Player
import com.FAMPE.fampe.repository.GameObjectRepository
import com.FAMPE.fampe.repository.PlayerRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {
    private val repository = PlayerRepository()
    private val objRepository = GameObjectRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _currentSessionId = MutableStateFlow("initial_session")
    val currentSessionId: StateFlow<String> = _currentSessionId

    init {
        listenToSession()
    }

    private fun listenToSession() {
        db.collection("globalSession")
            .document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MapViewModel", "Error listening to global session", error)
                    return@addSnapshotListener
                }

                val sessionId = snapshot?.getString("sessionId")
                if (sessionId != null) {
                    _currentSessionId.value = sessionId
                    Log.d("MapViewModel", "Session updated to: $sessionId")
                }
            }
    }

    fun updateLocation(userId: String, lat: Double, lng: Double) {
        repository.updateLocation(userId, lat, lng)

        objRepository.ensureObjectsNearPlayer(
            lat,
            lng,
            _currentSessionId.value
        )
    }

    fun listenToPlayers(onUpdate: (List<Player>) -> Unit) {
        db.collection("players")
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
                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null
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

                onUpdate(players)
            }
    }

    fun listenToObjects(onUpdate: (List<GameObject>) -> Unit) {
        // We filter by currentSessionId to only show objects for the active session
        // Note: You might need a composite index in Firestore if you add more filters
        db.collection("objects")
            .whereEqualTo("sessionId", _currentSessionId.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MapViewModel", "Error listening to objects", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val objects = snapshot.documents.mapNotNull { doc ->
                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null

                    GameObject(
                        id = doc.id,
                        sessionId = doc.getString("sessionId") ?: "",
                        location = location,
                        active = doc.getBoolean("active") ?: true,
                        points = doc.getLong("points")?.toInt() ?: 0
                    )
                }

                onUpdate(objects)
            }
    }
}
