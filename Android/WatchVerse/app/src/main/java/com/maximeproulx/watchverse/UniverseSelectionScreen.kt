package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SelectionGold = Color(0xFFDEBD47)

@Composable
fun UniverseSelectionScreen(
    availableUniverses: List<Universe>,
    onUniverseClick: (Universe) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.appbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choose Your Universe",
                    color = SelectionGold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Choose the universe you want to explore next.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(SelectionGold.copy(alpha = 0.45f))
                    )
                    Text("✦", color = SelectionGold, fontSize = 14.sp)
                    Spacer(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(SelectionGold.copy(alpha = 0.45f))
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                availableUniverses.chunked(2).forEach { rowUniverses ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowUniverses.forEach { universe ->
                            AvailableUniverseCard(
                                universe = universe,
                                isCurrent = false,
                                onClick = { onUniverseClick(universe) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowUniverses.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Text(
                text = "You can change universes anytime.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
