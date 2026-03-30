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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import android.util.Log
import com.FAMPE.fampe.model.GameObject
import com.FAMPE.fampe.model.Player
import com.FAMPE.fampe.service.GameObjectService
import com.FAMPE.fampe.location.LocationService
import com.FAMPE.fampe.viewmodel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.FAMPE.fampe.R
import com.google.android.gms.maps.CameraUpdateFactory

@Composable
fun MapScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    val gameObjectService = remember { GameObjectService() }
    val locationService = remember { LocationService(context) }

    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var objects by remember { mutableStateOf<List<GameObject>>(emptyList()) }


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

    LaunchedEffect(Unit) {

        viewModel.listenToPlayers {
            players = it
            Log.d("MapScreen", "Players list updated: ${it.size} players")
        }


        viewModel.listenToObjects {
            objects = it
            Log.d("MapScreen", "Objects list updated: ${it.size} objects")
        }
    }

    DisposableEffect(hasPermission, currentUser) {
        if (!hasPermission || currentUser == null) {
            onDispose { }
        } else {
            var hasMovedCamera = false

            locationService.startLocationUpdates { lat, lng ->
                Log.d("MapScreen", "New location: $lat, $lng")

                val p = LatLng(lat, lng)
                myPos = p

                currentUser?.uid?.let { userId ->
                    viewModel.updateLocation(userId, lat, lng)
                }

                if (!hasMovedCamera) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(p, 16f)
                    )
                    hasMovedCamera = true
                }
            }

            onDispose {
                locationService.stopLocationUpdates()
            }
        }
    }

    // Try to pick up nearby objects when position or objects change
    LaunchedEffect(myPos, objects, currentUser) {
        val pos = myPos
        val uid = currentUser?.uid

        if (pos != null && uid != null) {
            objects.forEach { obj ->
                gameObjectService.tryPickupObject(
                    pos.latitude,
                    pos.longitude,
                    obj,
                    uid
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasPermission
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = hasPermission
        )
    ) {

        objects.forEach { obj ->
            if (obj.active) {
                val pos = LatLng(
                    obj.location.latitude,
                    obj.location.longitude
                )
                val context = LocalContext.current
                val customIconObj = remember {
                    val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.chest_unopened)    // Force it to a specific size (e.g. 120px)
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 120, 120, false)
                    BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                }

                Marker(
                    state = MarkerState(position = pos),
                    title = "Treasure",
                    snippet = "+${obj.points} poäng",
                    icon = customIconObj
                )
            }
        }

        myPos?.let { pos ->
            val context = LocalContext.current
            val customIcon = remember {
                val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.pumpkin_character)    // Force it to a specific size (e.g. 120px)
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 170, 170, false)
                BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            }
            Marker(
                state = MarkerState(position = pos),
                title = "Jag",
                icon = customIcon
            )
        }
        players.forEach { player ->

            if (player.id != currentUser?.uid) {

                val pos = LatLng(
                    player.location.latitude,
                    player.location.longitude
                )
                val context = LocalContext.current
                val customIconPlayer = remember {
                    val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.longleg_man)    // Force it to a specific size (e.g. 120px)
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 170, 170, false)
                    BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                }


                Marker(
                    state = MarkerState(position = pos),
                    title = player.name,
                    icon = customIconPlayer
                    )



            }

        }
    }
}
