package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MovieDetailGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun MovieDetailScreen(
    movie: Movie,
    isWatched: Boolean,
    isSkipped: Boolean,
    onMarkWatched: () -> Unit = {},
    onSkip: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current

    val posterName = movie.poster
        .substringBeforeLast(".")
        .replace("-", "_")

    val posterResId = context.resources.getIdentifier(
        posterName,
        "drawable",
        context.packageName
    )

    val posterResource =
        if (posterResId != 0) {
            posterResId
        } else {
            R.drawable.placeholder_poster
        }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(posterResource),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(28.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(
                    top = 24.dp,
                    bottom = 120.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.55f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "✕",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Image(
                painter = painterResource(posterResource),
                contentDescription = movie.title,
                modifier = Modifier
                    .size(
                        width = 210.dp,
                        height = 315.dp
                    )
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${movie.year}  •  ${movie.runtime}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 15.sp
            )

            // Badge will eventually go here beside / with the metadata.

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onMarkWatched,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isWatched) {
                            Color(0xFF48C774)
                        } else {
                            MovieDetailGold
                        },
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text =
                        if (isWatched) {
                            "Watched"
                        } else {
                            "Mark as Watched"
                        },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!isWatched) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor =
                            if (isSkipped) {
                                MovieDetailGold
                            } else {
                                Color.White
                            }
                    )
                ) {
                    Text(
                        text =
                            if (isSkipped) {
                                "Skipped"
                            } else {
                                "Skip"
                            },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(36.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Synopsis",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = movie.synopsis,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(
                    modifier = Modifier.height(34.dp)
                )

                Text(
                    text = "Director",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = movie.director,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 17.sp
                )

                Spacer(
                    modifier = Modifier.height(34.dp)
                )

                Text(
                    text = "Genres",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    movie.genres.forEach { genre ->
                        Text(
                            text = genre,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 8.dp
                                )
                        )
                    }
                }
            }
        }
    }
}