package com.FAMPE.fampe.model

import com.google.firebase.firestore.GeoPoint

data class GameObject(
    val id: String = "",
    val sessionId: String = "",
    val active: Boolean = false,
    val foundBy: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val points: Int = 0,

    )
