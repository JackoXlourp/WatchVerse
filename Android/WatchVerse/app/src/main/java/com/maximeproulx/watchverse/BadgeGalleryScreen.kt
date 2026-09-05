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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BadgeGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun BadgeGalleryScreen(
    currentUser: WatchVerseUser,
    scrollTargetBadgeID: String? = null,
    onScrollTargetConsumed: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFullScreenOverlayChanged: (Boolean) -> Unit = {}
) {
    var selectedBadge by remember {
        mutableStateOf<Badge?>(null)
    }

    val badges = BadgeData.all
        .filter { badge ->
            badge.id != "founder" || currentUser.isFounder
        }
        .map { badge ->
            badge.copy(
                isUnlocked =
                    if (badge.id == "founder") {
                        true
                    } else {
                        currentUser.unlockedBadges.contains(badge.id)
                    }
            )
        }

    val groupedBadges = badges.groupBy { badge ->
        badge.universe
    }

    val universes = groupedBadges.keys.sortedWith { first, second ->
        when {
            first == "WATCHVERSE" -> -1
            second == "WATCHVERSE" -> 1
            else -> first.compareTo(second)
        }
    }

    val galleryRows = universes.flatMapIndexed { universeIndex, universe ->
        val universeBadges = groupedBadges[universe].orEmpty()
        listOf(BadgeGalleryRow.Section(universe, universeIndex == 0)) +
                universeBadges.chunked(3).mapIndexed { index, rowBadges ->
                    BadgeGalleryRow.Badges(
                        universe = universe,
                        badges = rowBadges,
                        isLastInSection = index == (universeBadges.size - 1) / 3
                    )
                }
    }
    val galleryListState = rememberLazyListState()

    LaunchedEffect(scrollTargetBadgeID, galleryRows) {
        val targetID = scrollTargetBadgeID ?: return@LaunchedEffect
        val targetIndex = galleryRows.indexOfFirst { row ->
            row is BadgeGalleryRow.Badges && row.badges.any { badge -> badge.id == targetID }
        }

        if (targetIndex >= 0) {
            galleryListState.animateScrollToItem(
                index = targetIndex,
                scrollOffset = -120
            )
            onScrollTargetConsumed()
        }
    }

    val context = LocalContext.current
    val allMovies = remember(context) {
        JSONLoader.loadUniverseSummaries(context).flatMap { summary ->
            JSONLoader.loadUniverse(
                context = context,
                fileName = "${summary.file}.json"
            ).movies
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.appbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            state = galleryListState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 150.dp),
        ) {
            item(key = "gallery-top-spacing") {
                Spacer(modifier = Modifier.height(50.dp))
            }

            itemsIndexed(
                items = galleryRows,
                key = { index, row ->
                    when (row) {
                        is BadgeGalleryRow.Section -> "section-${row.title}"
                        is BadgeGalleryRow.Badges -> "row-${row.universe}-$index"
                    }
                }
            ) { _, row ->
                when (row) {
                    is BadgeGalleryRow.Section -> {
                        Text(
                            text = row.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(
                                top = if (row.isFirst) 0.dp else 40.dp,
                                bottom = 16.dp
                            )
                        )
                    }

                    is BadgeGalleryRow.Badges -> {
                        BadgeGalleryGridRow(
                            badges = row.badges,
                            onBadgeClick = { badge ->
                                selectedBadge = badge
                                onFullScreenOverlayChanged(true)
                            },
                            modifier = Modifier.padding(
                                bottom = if (row.isLastInSection) 0.dp else 28.dp
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 20.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings_gear),
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        selectedBadge?.let { badge ->
            val badgeMovies = allMovies
                .filter { movie ->
                    badge.requiredMovieIDs.contains(movie.id)
                }
                .sortedBy { movie ->
                    currentUser.watchedMovies.contains(movie.id)
                }

            BadgeDetailScreen(
                badge = badge,
                movies = badgeMovies,
                currentUser = currentUser,
                onClose = {
                    selectedBadge = null
                    onFullScreenOverlayChanged(false)
                }
            )
        }
    }
}

private sealed interface BadgeGalleryRow {
    data class Section(
        val title: String,
        val isFirst: Boolean
    ) : BadgeGalleryRow

    data class Badges(
        val universe: String,
        val badges: List<Badge>,
        val isLastInSection: Boolean
    ) : BadgeGalleryRow
}

@Composable
private fun BadgeGalleryGridRow(
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top
    ) {
        badges.forEach { badge ->
            BadgeGalleryCard(
                badge = badge,
                onClick = { onBadgeClick(badge) },
                modifier = Modifier.weight(1f)
            )
        }

        repeat(3 - badges.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BadgeGalleryCard(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageResource = context.resources.getIdentifier(
        badge.imageName,
        "drawable",
        context.packageName
    )

    val grayscale = remember(badge.isUnlocked) {
        ColorMatrix().apply {
            setToSaturation(if (badge.isUnlocked) 1f else 0f)
        }
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = badge.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.colorMatrix(grayscale)
            )

            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BadgeGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = badge.title,
            color = if (badge.isUnlocked) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
