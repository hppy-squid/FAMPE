package com.FAMPE.fampe.repository

import com.FAMPE.fampe.firebase.FirestoreService

class PlayerRepository {
    fun updateLocation(userId: String, lat: Double, lng: Double) {
        FirestoreService.updatePlayerLocation(userId, lat, lng)
    }
}