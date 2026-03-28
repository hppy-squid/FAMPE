package com.FAMPE.fampe.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.FAMPE.fampe.model.Player
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = FirebaseFirestore.getInstance()
            .collection("players")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                players = snapshot.documents.mapNotNull { doc ->
                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null

                    Player(
                        id = doc.id,
                        location = location,
                        lastUpdated = doc.getTimestamp("lastUpdated")?.toDate()?.time ?: 0L,
                        name = doc.getString("name") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0
                    )
                }.sortedByDescending { it.score }
            }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Leaderboard")

        players.forEachIndexed { index, player ->
            Text(
                text = "${index + 1}. ${player.name.ifBlank { player.id }} - ${player.score} poäng",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}