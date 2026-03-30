package com.FAMPE.fampe.service

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun PlayerNameDialog() {
    val db = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var showNameDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }

    // Listen for auth state changes to update the user
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            user = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    // Check if the user has a name in Firestore
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("players")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists() || doc.getString("name").isNullOrEmpty()) {
                        showNameDialog = true
                    }
                }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing without name if desired */ },
            title = { Text("Välj användarnamn") },
            text = {
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Namn") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    enabled = username.isNotBlank(),
                    onClick = {
                        user?.uid?.let { uid ->
                            db.collection("players")
                                .document(uid)
                                .set(
                                    mapOf(
                                        "name" to username,
                                        "score" to 0
                                    ),
                                    SetOptions.merge()
                                )
                                .addOnSuccessListener {
                                    showNameDialog = false
                                }
                        }
                    }
                ) {
                    Text("Spara")
                }
            }
        )
    }
}
