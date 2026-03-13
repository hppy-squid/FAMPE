package com.FAMPE.fampe.model

import com.google.firebase.firestore.GeoPoint

data class Player(
    val id: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val lastUpdated: Long = 0,
    val name: String = "",
    val score: Int = 0
)
