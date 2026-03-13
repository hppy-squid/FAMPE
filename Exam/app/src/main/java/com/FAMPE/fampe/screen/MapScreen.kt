package com.FAMPE.fampe.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import android.util.Log
import com.FAMPE.fampe.model.Player
import com.FAMPE.fampe.viewmodel.MapViewModel

@Composable
fun MapScreen() {

    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }

    // Authenticate anonymously if not logged in
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { result ->
                    currentUser = result.user
                    Log.d("MapScreen", "Anonymous auth successful: ${result.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e("MapScreen", "Anonymous auth failed", e)
                }
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    var myPos by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState()

    // Ask permission once
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch location (only after auth completes)
    LaunchedEffect(hasPermission, currentUser) {

        if (!hasPermission || myPos != null || currentUser == null) return@LaunchedEffect

        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { loc ->

            loc?.let {

                val p = LatLng(it.latitude, it.longitude)

                myPos = p

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(p, 16f)
                )

                // Send location to Firebase
                currentUser?.uid?.let { userId ->
                    Log.d("MapScreen", "Sending location to Firebase: $userId, ${it.latitude}, ${it.longitude}")
                    viewModel.updateLocation(
                        userId,
                        it.latitude,
                        it.longitude
                    )
                } ?: run {
                    Log.w("MapScreen", "No authenticated user, location not sent")
                }
            }
        }

    }

    LaunchedEffect(Unit) {

        viewModel.listenToPlayers {
            players = it
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasPermission
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = hasPermission
        )
    ) {

        myPos?.let { pos ->
            Marker(
                state = MarkerState(position = pos),
                title = "Jag"
            )
        }
        players.forEach { player ->

            if (player.id != currentUser?.uid) {

                    val pos = LatLng(
                        player.location.latitude,
                        player.location.longitude
                    )

                Marker(
                    state = MarkerState(position = pos),
                    title = player.id
                )

            }

        }
    }
}