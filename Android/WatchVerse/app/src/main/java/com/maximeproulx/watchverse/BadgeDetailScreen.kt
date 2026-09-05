package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BadgeDetailGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun BadgeDetailScreen(
    badge: Badge,
    movies: List<Movie>,
    currentUser: WatchVerseUser,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val imageResource = context.resources.getIdentifier(
        badge.imageName,
        "drawable",
        context.packageName
    )
    val completedMovies = movies.count { movie ->
        currentUser.watchedMovies.contains(movie.id)
    }
    val isCompleted = movies.isNotEmpty() && completedMovies == movies.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = badge.title,
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = badge.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (movies.isNotEmpty()) {
                Text(
                    text = "$completedMovies / ${movies.size} completed",
                    color = Color.Gray,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        color = BadgeDetailGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed",
                        color = BadgeDetailGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = badge.description,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            if (movies.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        bottom = 80.dp
                    )
                ) {
                    item {
                        Text(
                            text = "Movies",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    items(
                        items = movies,
                        key = { movie -> movie.id }
                    ) { movie ->
                        val isWatched = currentUser.watchedMovies.contains(movie.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = movie.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (isWatched) "☑" else "☐",
                                color = if (isWatched) BadgeDetailGold else Color.Gray,
                                fontSize = 23.sp
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}
