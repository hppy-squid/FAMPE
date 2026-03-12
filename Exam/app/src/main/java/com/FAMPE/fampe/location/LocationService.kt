package com.FAMPE.fampe.location

import android.content.Context
import com.google.android.gms.location.LocationServices

class LocationService(private val context: Context){

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

}
