package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

private val WatchVerseGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun HomeScreen(
    onContinueWatchingClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeUniverse = JSONLoader.loadActiveUniverse(context)
    val universeSummaries = JSONLoader.loadUniverseSummaries(context)

    val comingSoonUniverses = universeSummaries.filter {
        it.state == "comingSoon"
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Background
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
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // MARK: WatchVerse title + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "WatchVerse",
                    color = WatchVerseGold,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color(0xFF2C2C2C).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "⚙",
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // MARK: Continue Watching
            Text(
                text = "Continue Watching",
                color = WatchVerseGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            HeroUniverseCard(
                universe = activeUniverse,
                onClick = onContinueWatchingClick
            )

            Spacer(modifier = Modifier.height(30.dp))

            // MARK: Coming Soon
            Text(
                text = "Coming Soon",
                color = WatchVerseGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                comingSoonUniverses
                    .chunked(2)
                    .forEach { rowUniverses ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowUniverses.forEach { summary ->

                                val universe = JSONLoader.loadUniverse(
                                    context = context,
                                    fileName = "${summary.file}.json"
                                )

                                ComingSoonCard(
                                    universe = universe,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (rowUniverses.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun HeroUniverseCard(
    universe: Universe,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val bannerName = universe.banner
        .substringBeforeLast(".")
        .replace("-", "_")

    val bannerRes = context.resources.getIdentifier(
        bannerName,
        "drawable",
        context.packageName
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(bannerRes),
            contentDescription = universe.fullTitle,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ComingSoonCard(
    universe: Universe,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val posterName = universe.poster
        .substringBeforeLast(".")
        .replace("-", "_")

    val posterRes = context.resources.getIdentifier(
        posterName,
        "drawable",
        context.packageName
    )

    Column(
        modifier = modifier
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(18.dp))
        ) {

            Image(
                painter = painterResource(
                    if (posterRes != 0) posterRes else R.drawable.placeholder_poster
                ),
                contentDescription = universe.fullTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Locked appearance
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )

            Image(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = "Locked",
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = universe.title,
            color = WatchVerseGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}