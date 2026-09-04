package com.maximeproulx.watchverse

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.absoluteValue

private val JourneyGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun JourneyScreen(
    universe: Universe,
    showReleaseYears: Boolean = true,
    onFilterClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onResetJourney: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    onFullScreenOverlayChanged: (Boolean) -> Unit = {},
) {
    var selectedFilters by remember {
        mutableStateOf(emptySet<String>())
    }
    var currentUser by remember {
        mutableStateOf<WatchVerseUser?>(null)
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        AuthenticationService.loadCurrentUser { user ->
            currentUser = user
        }
    }
    val movies =
        if (selectedFilters.isEmpty()) {
            universe.movies
        } else {
            universe.movies.filter { movie ->
                movie.tags.any { tag ->
                    selectedFilters.contains(tag)
                }
            }
        }
    var showFilterDropdown by remember {
        mutableStateOf(false)
    }
    var selectedMovie by remember {
        mutableStateOf<Movie?>(null)
    }

    if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No content available",
                color = Color.White
            )
        }

        return
    }

    val firstUnwatchedIndex =
        movies.indexOfFirst { movie ->
            currentUser?.watchedMovies?.contains(movie.id) != true &&
                    currentUser?.skippedMovies?.contains(movie.id) != true
        }.let {
            if (it == -1) movies.size else it
        }

    val pagerState = rememberPagerState(
        initialPage = firstUnwatchedIndex,
        pageCount = { movies.size + 1 }
    )
    var showSettings by remember {
        mutableStateOf(false)
    }
    androidx.compose.runtime.LaunchedEffect(selectedFilters) {
        val targetIndex = movies.indexOfFirst {
            !it.isWatched && !it.isSkipped
        }.let {
            if (it == -1) 0 else it
        }

        pagerState.scrollToPage(targetIndex)
    }
    androidx.compose.runtime.LaunchedEffect(
        currentUser?.watchedMovies,
        currentUser?.skippedMovies
    ) {
        val watchedMovies = currentUser?.watchedMovies ?: emptyList()
        val skippedMovies = currentUser?.skippedMovies ?: emptyList()

        val targetIndex = movies.indexOfFirst { movie ->
            !watchedMovies.contains(movie.id) &&
                    !skippedMovies.contains(movie.id)
        }.let {
            if (it == -1) movies.size else it
        }

        pagerState.animateScrollToPage(targetIndex)
    }

    val watchedMovies = currentUser?.watchedMovies ?: emptyList()

    val watchedCount = movies.count { movie ->
        watchedMovies.contains(movie.id)
    }

    val journeyComplete =
        movies.isNotEmpty() &&
                watchedCount == movies.size

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                .padding(bottom = 110.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // Universe title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Filter button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
                        .clickable {
                            showFilterDropdown = true
                            onFilterClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☷",
                        color = Color.White,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Universe title
                Text(
                    text = universe.title,
                    color = JourneyGold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color.White.copy(alpha = 0.65f))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = universe.subtitle,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Settings button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
                        .clickable {
                            showSettings = true
                            onFullScreenOverlayChanged(true)
                            onSettingsClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙",
                        color = Color.White,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Poster carousel
            HorizontalPager(
                state = pagerState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 82.dp
                ),
                pageSpacing = (-18).dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->

                val pageOffset =
                    (
                            (pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction
                            ).absoluteValue

                val centerProgress =
                    (1f - pageOffset.coerceIn(0f, 1f))

                if (page < movies.size) {
                    JourneyPosterCard(
                        movie = movies[page].copy(
                            isWatched = currentUser?.watchedMovies?.contains(movies[page].id) == true,
                            isSkipped = currentUser?.skippedMovies?.contains(movies[page].id) == true
                        ),
                        centerProgress = centerProgress,
                        onClick = {
                            selectedMovie = movies[page]
                            onFullScreenOverlayChanged(true)
                            onMovieClick(movies[page])
                        }
                    )
                } else {
                    JourneyEndCard(
                        universe = universe,
                        journeyComplete = journeyComplete,
                        centerProgress = centerProgress,
                        onClick = onResetJourney
                    )
                }
            }

            // Current movie information
            if (
                pagerState.currentPage < movies.size &&
                !journeyComplete
            ) {
                val movie = movies[pagerState.currentPage].copy(
                    isWatched = currentUser?.watchedMovies?.contains(movies[pagerState.currentPage].id) == true,
                    isSkipped = currentUser?.skippedMovies?.contains(movies[pagerState.currentPage].id) == true
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "CURRENT MOVIE",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text =
                            if (showReleaseYears) {
                                "${movie.year} • ${movie.runtime}"
                            } else {
                                movie.runtime
                            },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    MovieTagChip(movie)
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.White.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress
            if (journeyComplete) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "JOURNEY COMPLETED",
                        color = Color(0xFF48C774),
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = universe.fullTitle,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF48C774))
                            .padding(top = 3.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Congratulations!",
                        color = Color(0xFF48C774),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "You've completed ${universe.fullTitle}!",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
            } else {
                val percent =
                    if (movies.isNotEmpty()) {
                        ((watchedCount.toFloat() / movies.size) * 100).toInt()
                    } else {
                        0
                    }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "JOURNEY PROGRESS",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "$watchedCount of ${movies.size}",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$percent % Complete",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
        if (showFilterDropdown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        showFilterDropdown = false
                    }
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp, top = 70.dp)
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.TopStart
                ) {
                    FilterDropdown(
                        filters = universe.filters,
                        selectedFilters = selectedFilters,
                        onApply = { newSelection ->
                            selectedFilters = newSelection
                            showFilterDropdown = false
                        }
                    )
                }
            }
        }
        if (showSettings) {
            SettingsScreen(
                universe = universe,
                showReleaseYears = currentUser?.showReleaseYears ?: true,
                onShowReleaseYearsChanged = { value ->
                    AuthenticationService.updateShowReleaseYears(value) { success ->
                        if (success) {
                            currentUser = currentUser?.copy(
                                showReleaseYears = value
                            )
                        }
                    }
                },
                notifyNewUniverses = currentUser?.notifyNewUniverses ?: true,
                onNotifyNewUniversesChanged = { value ->
                    AuthenticationService.updateNotifyNewUniverses(value) { success ->
                        if (success) {
                            currentUser = currentUser?.copy(
                                notifyNewUniverses = value
                            )
                        }
                    }
                },
                displayName = currentUser?.displayName ?: "",
                accountSubtitle = currentUser?.let { user ->
                    val formattedDate =
                        java.text.SimpleDateFormat(
                            "MMMM yyyy",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(user.joinedDate))

                    if (user.isFounder) {
                        "WatchVerse Founder | $formattedDate"
                    } else {
                        "Joined | $formattedDate"
                    }
                } ?: "",
                onLogout = {
                    AuthenticationService.signOut()
                    showSettings = false
                    onSignedOut()
                },
                onClose = {
                    showSettings = false
                    onFullScreenOverlayChanged(false)
                },
                onResetUniverseProgress = {
                    AuthenticationService.resetUniverseProgress(
                        movieIds = universe.movies.map { it.id }
                    ) { success ->
                        if (success) {
                            currentUser = currentUser?.copy(
                                watchedMovies = currentUser
                                    ?.watchedMovies
                                    ?.filterNot { universe.movies.any { movie -> movie.id == it } }
                                    ?: emptyList(),
                                skippedMovies = currentUser
                                    ?.skippedMovies
                                    ?.filterNot { universe.movies.any { movie -> movie.id == it } }
                                    ?: emptyList()
                            )
                        }
                    }
                }
            )
        }
        selectedMovie?.let { movie ->
            MovieDetailScreen(
                movie = movie,
                isWatched = currentUser?.watchedMovies?.contains(movie.id) == true,
                isSkipped = currentUser?.skippedMovies?.contains(movie.id) == true,
                onMarkWatched = {
                    val updatedWatchedMovies =
                        (currentUser?.watchedMovies ?: emptyList())
                            .toMutableList()

                    if (updatedWatchedMovies.contains(movie.id)) {
                        updatedWatchedMovies.remove(movie.id)
                    } else {
                        updatedWatchedMovies.add(movie.id)
                    }

                    AuthenticationService.updateWatchedMovies(
                        updatedWatchedMovies
                    ) { success ->
                        if (success) {
                            currentUser = currentUser?.copy(
                                watchedMovies = updatedWatchedMovies
                            )

                            selectedMovie = null
                            onFullScreenOverlayChanged(false)
                        }
                    }
                },
                onSkip = {
                    val updatedSkippedMovies =
                        (currentUser?.skippedMovies ?: emptyList())
                            .toMutableList()

                    if (updatedSkippedMovies.contains(movie.id)) {
                        updatedSkippedMovies.remove(movie.id)
                    } else {
                        updatedSkippedMovies.add(movie.id)
                    }

                    AuthenticationService.updateSkippedMovies(
                        updatedSkippedMovies
                    ) { success ->
                        if (success) {
                            currentUser = currentUser?.copy(
                                skippedMovies = updatedSkippedMovies
                            )

                            selectedMovie = null
                            onFullScreenOverlayChanged(false)
                        }
                    }
                },
                onClose = {
                    selectedMovie = null
                    onFullScreenOverlayChanged(false)
                }
            )
        }
    }
}

@Composable
private fun JourneyPosterCard(
    movie: Movie,
    centerProgress: Float,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val posterName = movie.poster
        .substringBeforeLast(".")
        .replace("-", "_")

    val posterRes = context.resources.getIdentifier(
        posterName,
        "drawable",
        context.packageName
    )

    val scale by animateFloatAsState(
        targetValue = 0.68f + (0.32f * centerProgress),
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 350f
        ),
        label = "posterScale"
    )

    val cardWidth = 220.dp
    val cardHeight = 325.dp

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.66f + (0.34f * centerProgress)
            }
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val saturationMatrix = ColorMatrix().apply {
            setToSaturation(
                if (
                    movie.isWatched &&
                    centerProgress < 0.5f
                ) {
                    0f
                } else {
                    1f
                }
            )
        }

        Image(
            painter = painterResource(
                if (posterRes != 0) {
                    posterRes
                } else {
                    R.drawable.placeholder_poster
                }
            ),
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(saturationMatrix)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width =
                        if (
                            movie.isWatched &&
                            centerProgress > 0.5f
                        ) {
                            3.dp
                        } else {
                            2.dp
                        },
                    color =
                        when {
                            movie.isWatched &&
                                    centerProgress > 0.5f ->
                                Color(0xFF48C774)

                            centerProgress > 0.5f ->
                                JourneyGold

                            else ->
                                Color.White.copy(alpha = 0.12f)
                        },
                    shape = RoundedCornerShape(20.dp)
                )
        )

        if (movie.isWatched) {
            Text(
                text = "✓",
                color =
                    if (centerProgress > 0.5f) {
                        Color.White
                    } else {
                        Color.Gray
                    },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (centerProgress > 0.5f) {
                            Color(0xFF48C774)
                        } else {
                            Color.Black.copy(alpha = 0.60f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun JourneyEndCard(
    universe: Universe,
    journeyComplete: Boolean,
    centerProgress: Float,
    onClick: () -> Unit
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

    val scale by animateFloatAsState(
        targetValue = 0.68f + (0.32f * centerProgress),
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 350f
        ),
        label = "endScale"
    )

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(325.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.66f + (0.34f * centerProgress)
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                if (posterRes != 0) {
                    posterRes
                } else {
                    R.drawable.placeholder_poster
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(5.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "▣",
                color = Color.White,
                fontSize = 36.sp
            )

            Text(
                text = "The End",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            if (journeyComplete) {
                Text(
                    text = "You've completed",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp
                )

                Text(
                    text = universe.fullTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.30f)
            )

            Text(
                text = "↻  Start Journey Again",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MovieTagChip(
    movie: Movie
) {
    val visibleTag = movie.tags
        .firstOrNull {
            it != "others"
        }

    val label = when (visibleTag) {
        "infinity_saga" -> "Infinity Saga"
        "multiverse_saga" -> "Multiverse Saga"
        null -> "No Badge"
        else -> visibleTag
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    it.uppercase()
                }
            }
    }

    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
    )
}