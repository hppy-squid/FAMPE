package com.FAMPE.fampe.viewmodel

import androidx.lifecycle.ViewModel
import com.FAMPE.fampe.repository.PlayerRepository

class MapViewModel  : ViewModel() {
    private val repository = PlayerRepository()

    fun updateLocation(userId: String, lat: Double, lng: Double) {
        repository.updateLocation(userId, lat, lng)
    }
}