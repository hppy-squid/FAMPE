package com.FAMPE.fampe

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*

@Composable
fun MapScreen() {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    var myPos by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect

        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val p = LatLng(loc.latitude, loc.longitude)
                    myPos = p
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(p, 16f))
                }
            }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasPermission),
        uiSettings = MapUiSettings(myLocationButtonEnabled = hasPermission)
    ) {
        myPos?.let { pos ->
            Marker(state = MarkerState(position = pos), title = "Jag")
        }
    }
}