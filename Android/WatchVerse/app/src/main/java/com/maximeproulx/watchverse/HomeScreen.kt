package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WatchVerseGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun HomeScreen(
    activeUniverse: Universe,
    availableUniverses: List<Universe>,
    comingSoonUniverses: List<Universe>,
    onContinueWatchingClick: () -> Unit = {},
    onUniverseClick: (Universe) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
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

            // MARK: WatchVerse title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "WatchVerse",
                    color = WatchVerseGold,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // MARK: Current Universe
            Text(
                text = "Current Universe",
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

            Text(
                text = "Your Universes",
                color = WatchVerseGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                availableUniverses
                    .filter { universe -> universe.id != activeUniverse.id }
                    .chunked(2)
                    .forEach { rowUniverses ->
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
                            rowUniverses.forEach { universe ->
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

            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 10.dp, end = 16.dp)
                .size(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF2C2C2C).copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_gear),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AvailableUniverseCard(
    universe: Universe,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = WatchVerseGold.copy(alpha = if (isCurrent) 0.9f else 0.35f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ArtworkImage(
                source = universe.poster,
                contentDescription = universe.fullTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        WatchVerseGold.copy(alpha = 0.8f),
                        RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop,
                placeholder = R.drawable.placeholder_poster
            )

            Text(
                text = universe.title,
                color = WatchVerseGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                modifier = Modifier.height(44.dp)
            )
        }

        if (isCurrent) {
            Text(
                text = "CURRENT",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(WatchVerseGold)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun HeroUniverseCard(
    universe: Universe,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable {
                onClick()
            }
            .padding(12.dp)
    ) {
        ArtworkImage(
            source = universe.banner,
            contentDescription = universe.fullTitle,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    WatchVerseGold.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                ),
            contentScale = ContentScale.Crop,
            placeholder = R.drawable.placeholder_poster
        )
    }
}

@Composable
private fun ComingSoonCard(
    universe: Universe,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                WatchVerseGold.copy(alpha = 0.35f),
                RoundedCornerShape(24.dp)
            )
            .padding(12.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    WatchVerseGold.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
        ) {

            ArtworkImage(
                source = universe.poster,
                contentDescription = universe.fullTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply {
                        setToSaturation(0f)
                    }
                ),
                placeholder = R.drawable.placeholder_poster
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = universe.title,
            color = WatchVerseGold,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            modifier = Modifier.height(44.dp)
        )
    }
}
