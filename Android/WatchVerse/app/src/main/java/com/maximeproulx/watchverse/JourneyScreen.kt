package com.maximeproulx.watchverse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

private val JourneyGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

private enum class CompletionStage {
    IDLE,
    GROWING,
    COMPLETED,
    SHRINKING
}

private data class JourneyCarouselMovie(
    val movie: Movie,
    val poster: String,
    val isWatched: Boolean,
    val isSkipped: Boolean
)

private fun resolveJourneyPosition(
    universeMovies: List<Movie>,
    filteredMovies: List<Movie>,
    savedMovieID: String?,
    watchedMovieIDs: Set<String>,
    skippedMovieIDs: Set<String>
): Int {
    val firstPendingIndex = filteredMovies.indexOfFirst { movie ->
        !watchedMovieIDs.contains(movie.id) &&
                !skippedMovieIDs.contains(movie.id)
    }

    if (savedMovieID == null) {
        return if (firstPendingIndex == -1) filteredMovies.size else firstPendingIndex
    }

    val exactIndex = filteredMovies.indexOfFirst { movie -> movie.id == savedMovieID }
    if (exactIndex != -1) {
        return exactIndex
    }

    val savedGlobalIndex = universeMovies.indexOfFirst { movie -> movie.id == savedMovieID }
    if (savedGlobalIndex == -1) {
        return if (firstPendingIndex == -1) filteredMovies.size else firstPendingIndex
    }

    val globalIndexes = universeMovies
        .mapIndexed { index, movie -> movie.id to index }
        .toMap()

    val nextFilteredIndex = filteredMovies.indexOfFirst { movie ->
        (globalIndexes[movie.id] ?: Int.MAX_VALUE) >= savedGlobalIndex
    }

    return if (nextFilteredIndex != -1) {
        nextFilteredIndex
    } else {
        filteredMovies.lastIndex.coerceAtLeast(0)
    }
}

@Composable
fun JourneyScreen(
    universe: Universe,
    badges: List<Badge>,
    currentUser: WatchVerseUser,
    onCurrentUserChanged: (WatchVerseUser) -> Unit,
    onFilterClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onResetJourney: () -> Unit = {},
    onBadgesUnlocked: (List<Badge>) -> Unit = {},
    onFullScreenOverlayChanged: (Boolean) -> Unit = {},
) {
    var selectedFilters by remember(currentUser.selectedUniverseFilters, universe.id) {
        mutableStateOf(
            currentUser.selectedUniverseFilters[universe.id]
                ?.toSet()
                ?: emptySet()
        )
    }
    val movies = remember(universe.movies, selectedFilters) {
        if (selectedFilters.isEmpty()) {
            universe.movies
        } else {
            universe.movies.filter { movie ->
                movie.tags.any { tag ->
                    selectedFilters.contains(tag)
                }
            }
        }
    }
    val watchedMovieIDs = remember(currentUser.watchedMovies) {
        currentUser.watchedMovies.toSet()
    }
    val skippedMovieIDs = remember(currentUser.skippedMovies) {
        currentUser.skippedMovies.toSet()
    }
    val latestCurrentUser = androidx.compose.runtime.rememberUpdatedState(currentUser)
    val carouselMovies = remember(
        movies,
        watchedMovieIDs,
        skippedMovieIDs
    ) {
        movies.map { movie ->
            JourneyCarouselMovie(
                movie = movie,
                poster = movie.poster,
                isWatched = watchedMovieIDs.contains(movie.id),
                isSkipped = skippedMovieIDs.contains(movie.id)
            )
        }
    }
    var showFilterDropdown by remember {
        mutableStateOf(false)
    }
    var selectedMovie by remember {
        mutableStateOf<Movie?>(null)
    }
    var showMovieDetail by remember {
        mutableStateOf(false)
    }
    var selectedBadge by remember {
        mutableStateOf<Badge?>(null)
    }
    var completionStage by remember {
        mutableStateOf(CompletionStage.IDLE)
    }
    var currentAnimationMovieID by remember {
        mutableStateOf<String?>(null)
    }
    val animationScope = rememberCoroutineScope()

    fun dismissMovieDetail() {
        showMovieDetail = false
        onFullScreenOverlayChanged(false)

        animationScope.launch {
            delay(300)
            if (!showMovieDetail) {
                selectedMovie = null
            }
        }
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

    val savedJourneyMovieID = currentUser.journeyPositions[universe.id]
    val initialJourneyIndex = remember(universe.id) {
        resolveJourneyPosition(
            universeMovies = universe.movies,
            filteredMovies = movies,
            savedMovieID = savedJourneyMovieID,
            watchedMovieIDs = watchedMovieIDs,
            skippedMovieIDs = skippedMovieIDs
        )
    }

    var currentIndex by remember(universe.id) {
        mutableIntStateOf(initialJourneyIndex)
    }
    var browsingMovieID by remember(universe.id) {
        mutableStateOf(
            movies.getOrNull(initialJourneyIndex)?.id ?: savedJourneyMovieID
        )
    }
    var dragOffsetPx by remember {
        mutableFloatStateOf(0f)
    }
    var carouselTransitionInProgress by remember {
        mutableStateOf(false)
    }
    val carouselPosition = remember(universe.id) {
        Animatable(initialJourneyIndex.toFloat())
    }

    fun saveJourneyPosition(movieID: String) {
        browsingMovieID = movieID

        val user = latestCurrentUser.value
        if (user.journeyPositions[universe.id] == movieID) {
            return
        }

        val updatedPositions = user.journeyPositions + (universe.id to movieID)
        onCurrentUserChanged(user.copy(journeyPositions = updatedPositions))
        AuthenticationService.updateJourneyPositions(updatedPositions)
    }

    suspend fun animateCarouselTo(
        targetIndex: Int,
        stiffness: Float = 247f
    ) {
        val boundedTarget = targetIndex.coerceIn(0, movies.size)
        carouselTransitionInProgress = true
        dragOffsetPx = 0f
        currentIndex = boundedTarget

        try {
            carouselPosition.animateTo(
                targetValue = boundedTarget.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = stiffness
                )
            )
        } finally {
            carouselTransitionInProgress = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(movies) {
        val targetIndex = resolveJourneyPosition(
            universeMovies = universe.movies,
            filteredMovies = movies,
            savedMovieID = browsingMovieID,
            watchedMovieIDs = watchedMovieIDs,
            skippedMovieIDs = skippedMovieIDs
        )

        currentIndex = targetIndex
        carouselPosition.snapTo(targetIndex.toFloat())
    }

    val completedCount = universe.movies.count { movie ->
        watchedMovieIDs.contains(movie.id) ||
                skippedMovieIDs.contains(movie.id)
    }

    val journeyComplete =
        universe.movies.isNotEmpty() &&
                completedCount == universe.movies.size

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

            Spacer(modifier = Modifier.height(72.dp))

            // Universe title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Poster carousel
            JourneyCarousel(
                movies = carouselMovies,
                universe = universe,
                endPoster = universe.poster,
                journeyComplete = journeyComplete,
                currentIndex = currentIndex,
                carouselPosition = carouselPosition,
                dragOffsetPx = dragOffsetPx,
                transitionInProgress = carouselTransitionInProgress,
                completionStage = completionStage,
                currentAnimationMovieID = currentAnimationMovieID,
                onDragOffsetChanged = { offset ->
                    dragOffsetPx = offset
                },
                onDragSettled = { startPosition, targetIndex ->
                    animationScope.launch {
                        carouselTransitionInProgress = true
                        carouselPosition.snapTo(startPosition)
                        dragOffsetPx = 0f
                        currentIndex = targetIndex

                        try {
                            carouselPosition.animateTo(
                                targetValue = targetIndex.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = 0.8f,
                                    stiffness = 247f
                                )
                            )
                        } finally {
                            carouselTransitionInProgress = false
                        }

                        movies.getOrNull(targetIndex)?.let { movie ->
                            saveJourneyPosition(movie.id)
                        }
                    }
                },
                onMovieClick = { index, movie ->
                    if (index == currentIndex) {
                        selectedMovie = movie
                        showMovieDetail = true
                        onFullScreenOverlayChanged(true)
                        onMovieClick(movie)
                    } else if (!carouselTransitionInProgress) {
                        animationScope.launch {
                            animateCarouselTo(index, stiffness = 322f)
                            saveJourneyPosition(movie.id)
                        }
                    }
                },
                onEndClick = onResetJourney
            )

            // Current movie information
            if (
                currentIndex < movies.size &&
                !journeyComplete
            ) {
                val movie = movies[currentIndex].copy(
                    isWatched = currentUser.watchedMovies.contains(movies[currentIndex].id),
                    isSkipped = currentUser.skippedMovies.contains(movies[currentIndex].id)
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
                            if (currentUser.showReleaseYears) {
                                "${movie.year} • ${movie.runtime}"
                            } else {
                                movie.runtime
                            },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    val movieBadges = badges.filter { badge ->
                        badge.requiredContentIDs.contains(movie.id)
                    }

                    if (movieBadges.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            movieBadges.forEach { badge ->
                                MovieBadgeCapsule(
                                    badge = badge,
                                    onClick = {
                                        selectedBadge = badge
                                        onFullScreenOverlayChanged(true)
                                    }
                                )
                            }
                        }
                    }
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
                        ((completedCount.toFloat() / universe.movies.size) * 100).toInt()
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
                        text = "$completedCount of ${universe.movies.size}",
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

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp)
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

                            val user = latestCurrentUser.value
                            val updatedFilters = user.selectedUniverseFilters +
                                (universe.id to newSelection.toList())

                            onCurrentUserChanged(
                                user.copy(selectedUniverseFilters = updatedFilters)
                            )

                            AuthenticationService.updateSelectedUniverseFilters(
                                updatedFilters
                            )

                            showFilterDropdown = false
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showMovieDetail,
            enter = EnterTransition.None,
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            selectedMovie?.let { movie ->
                MovieDetailScreen(
                    movie = movie,
                    badges = badges,
                    isWatched = currentUser.watchedMovies.contains(movie.id),
                    isSkipped = currentUser.skippedMovies.contains(movie.id),
                    showReleaseYears = currentUser.showReleaseYears,
                    onBadgeClick = { badge ->
                        selectedBadge = badge
                    },
                    onMarkWatched = {
                        val updatedWatchedMovies =
                            currentUser.watchedMovies
                                .toMutableList()

                        if (updatedWatchedMovies.contains(movie.id)) {
                            updatedWatchedMovies.remove(movie.id)

                            AuthenticationService.updateWatchedMovies(
                                updatedWatchedMovies
                            ) { success ->
                                if (success) {
                                    onCurrentUserChanged(
                                        currentUser.copy(watchedMovies = updatedWatchedMovies)
                                    )
                                }
                            }

                            return@MovieDetailScreen
                        }

                        updatedWatchedMovies.add(movie.id)
                        val updatedSkippedMovies =
                            currentUser.skippedMovies.filterNot { skippedID ->
                                skippedID == movie.id
                            }

                        currentAnimationMovieID = movie.id
                        completionStage = CompletionStage.GROWING
                        dismissMovieDetail()

                        animationScope.launch {
                            var watchedPersistenceSucceeded = false
                            var completionAnimationFinished = false

                            fun finishBadgeUnlockEvaluation() {
                                val unlockResult = BadgeUnlockEvaluator.check(
                                    currentUser.copy(
                                        watchedMovies = updatedWatchedMovies,
                                        skippedMovies = updatedSkippedMovies
                                    ),
                                    badges = badges
                                )
                                onCurrentUserChanged(unlockResult.user)

                                if (unlockResult.newlyUnlockedBadges.isNotEmpty()) {
                                    AuthenticationService.updateUnlockedBadges(
                                        unlockResult.user.unlockedBadges
                                    )
                                    onBadgesUnlocked(unlockResult.newlyUnlockedBadges)
                                }
                            }

                            delay(450)

                            val watchedUser = currentUser.copy(
                                watchedMovies = updatedWatchedMovies,
                                skippedMovies = updatedSkippedMovies
                            )
                            onCurrentUserChanged(watchedUser)
                            completionStage = CompletionStage.COMPLETED

                            AuthenticationService.updateSkippedMovies(updatedSkippedMovies)
                            AuthenticationService.updateWatchedMovies(
                                updatedWatchedMovies
                            ) { success ->
                                if (success) {
                                    watchedPersistenceSucceeded = true

                                    if (completionAnimationFinished) {
                                        finishBadgeUnlockEvaluation()
                                    }
                                }
                            }

                            delay(250)
                            completionStage = CompletionStage.SHRINKING

                            delay(450)
                            completionStage = CompletionStage.IDLE
                            currentAnimationMovieID = null

                            val completedIndex = movies.indexOfFirst { candidate ->
                                candidate.id == movie.id
                            }.takeIf { index -> index >= 0 }
                                ?: currentIndex.coerceAtMost(movies.lastIndex)
                            val targetIndex = movies.indices
                                .firstOrNull { index ->
                                    index > completedIndex &&
                                            !updatedWatchedMovies.contains(movies[index].id) &&
                                            !updatedSkippedMovies.contains(movies[index].id)
                                }
                                ?: movies.size

                            animateCarouselTo(targetIndex)
                            saveJourneyPosition(
                                movies.getOrNull(targetIndex)?.id ?: movie.id
                            )

                            completionAnimationFinished = true

                            if (watchedPersistenceSucceeded) {
                                finishBadgeUnlockEvaluation()
                            }
                        }
                    },
                    onSkip = {
                        val updatedSkippedMovies =
                            currentUser.skippedMovies
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
                                onCurrentUserChanged(currentUser.copy(
                                    skippedMovies = updatedSkippedMovies
                                ))

                                dismissMovieDetail()

                                animationScope.launch {
                                    delay(500)

                                    val skippedIndex = movies.indexOfFirst { candidate ->
                                        candidate.id == movie.id
                                    }.takeIf { index -> index >= 0 }
                                        ?: currentIndex.coerceAtMost(movies.lastIndex)
                                    val targetIndex = movies.indices
                                        .firstOrNull { index ->
                                            index > skippedIndex &&
                                                !currentUser.watchedMovies.contains(movies[index].id) &&
                                                !updatedSkippedMovies.contains(movies[index].id)
                                        }
                                        ?: movies.size

                                    animateCarouselTo(targetIndex)
                                    saveJourneyPosition(
                                        movies.getOrNull(targetIndex)?.id ?: movie.id
                                    )
                                }
                            }
                        }
                    },
                    onClose = {
                        dismissMovieDetail()
                    }
                )
            }
        }

        selectedBadge?.let { badge ->
            BadgeDetailScreen(
                badge = badge,
                movies = universe.movies.filter { movie ->
                    badge.requiredContentIDs.contains(movie.id)
                },
                currentUser = currentUser,
                onClose = {
                    selectedBadge = null
                    onFullScreenOverlayChanged(showMovieDetail)
                }
            )
        }
    }
}

@Composable
private fun JourneyCarousel(
    movies: List<JourneyCarouselMovie>,
    universe: Universe,
    endPoster: String,
    journeyComplete: Boolean,
    currentIndex: Int,
    carouselPosition: Animatable<Float, *>,
    dragOffsetPx: Float,
    transitionInProgress: Boolean,
    completionStage: CompletionStage,
    currentAnimationMovieID: String?,
    onDragOffsetChanged: (Float) -> Unit,
    onDragSettled: (Float, Int) -> Unit,
    onMovieClick: (Int, Movie) -> Unit,
    onEndClick: () -> Unit
) {
    val density = LocalDensity.current
    val posterSpacingPx = with(density) { 190.dp.toPx() }
    val movementThresholdPx = with(density) { 120.dp.toPx() }
    val visualPosition = carouselPosition.value - (dragOffsetPx / posterSpacingPx)
    val firstVisibleIndex = (floor(visualPosition).toInt() - 2).coerceAtLeast(0)
    val lastVisibleIndex = (ceil(visualPosition).toInt() + 2).coerceAtMost(movies.size)
    val velocityTracker = remember { VelocityTracker() }
    val gesturesEnabled =
        !transitionInProgress && completionStage == CompletionStage.IDLE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(325.dp)
            .pointerInput(gesturesEnabled, currentIndex, movies.size) {
                if (!gesturesEnabled) {
                    return@pointerInput
                }

                var accumulatedDrag = 0f

                detectHorizontalDragGestures(
                    onDragStart = {
                        accumulatedDrag = 0f
                        velocityTracker.resetTracking()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                        velocityTracker.addPosition(
                            timeMillis = change.uptimeMillis,
                            position = change.position
                        )
                        onDragOffsetChanged(accumulatedDrag)
                    },
                    onDragCancel = {
                        val startPosition =
                            carouselPosition.value -
                                    (accumulatedDrag / posterSpacingPx)
                        onDragSettled(startPosition, currentIndex)
                    },
                    onDragEnd = {
                        val horizontalVelocity = velocityTracker.calculateVelocity().x
                        val projectedTranslation =
                            accumulatedDrag + (horizontalVelocity * 0.05f)
                        val movement =
                            (-projectedTranslation / movementThresholdPx).roundToInt()
                        val targetIndex =
                            (currentIndex + movement).coerceIn(0, movies.size)
                        val startPosition =
                            carouselPosition.value -
                                    (accumulatedDrag / posterSpacingPx)

                        onDragSettled(startPosition, targetIndex)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (firstVisibleIndex <= lastVisibleIndex) {
            (firstVisibleIndex..lastVisibleIndex).forEach { index ->
                val itemOffsetPx = (index - visualPosition) * posterSpacingPx
                val centerProgress =
                    (1f - (abs(itemOffsetPx) / posterSpacingPx)).coerceIn(0f, 1f)
                val itemModifier = Modifier
                    .graphicsLayer {
                        translationX = itemOffsetPx
                    }
                    .zIndex(centerProgress)

                if (index < movies.size) {
                    val carouselMovie = movies[index]
                    val movie = carouselMovie.movie

                    key("journey-movie-${movie.id}") {
                        JourneyPosterCard(
                            movie = movie,
                            poster = carouselMovie.poster,
                            isWatched = carouselMovie.isWatched,
                            isSkipped = carouselMovie.isSkipped,
                            centerProgress = centerProgress,
                            completionStage =
                                if (currentAnimationMovieID == movie.id) {
                                    completionStage
                                } else {
                                    CompletionStage.IDLE
                                },
                            onClick = {
                                if (!transitionInProgress) {
                                    onMovieClick(index, movie)
                                }
                            },
                            modifier = itemModifier
                        )
                    }
                } else {
                    key("journey-end-${universe.id}") {
                        JourneyEndCard(
                            universe = universe,
                            poster = endPoster,
                            journeyComplete = journeyComplete,
                            centerProgress = centerProgress,
                            onClick = onEndClick,
                            modifier = itemModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyPosterCard(
    movie: Movie,
    poster: String,
    isWatched: Boolean,
    isSkipped: Boolean,
    centerProgress: Float,
    completionStage: CompletionStage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val carouselScale = 0.68f + (0.32f * centerProgress)
    val completionScale by animateFloatAsState(
        targetValue =
            if (
                completionStage == CompletionStage.GROWING ||
                completionStage == CompletionStage.COMPLETED
            ) {
                1.25f
            } else {
                1f
            },
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 322f
        ),
        label = "completionScale"
    )

    val cardWidth = 220.dp
    val cardHeight = 325.dp

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                scaleX = carouselScale * completionScale
                scaleY = carouselScale * completionScale
                alpha = 0.66f + (0.34f * centerProgress)
            }
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val isWatchedSidePoster = isWatched && centerProgress < 0.5f
        val saturationMatrix = remember(isWatchedSidePoster) {
            ColorMatrix().apply {
                setToSaturation(if (isWatchedSidePoster) 0f else 1f)
            }
        }

        ArtworkImage(
            source = poster,
            placeholder = R.drawable.placeholder_poster,
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
                            (isWatched || completionStage == CompletionStage.COMPLETED) &&
                            centerProgress > 0.5f
                        ) {
                            3.dp
                        } else {
                            2.dp
                        },
                    color =
                        when {
                            (isWatched || completionStage == CompletionStage.COMPLETED) &&
                                    centerProgress > 0.5f ->
                                Color(0xFF48C774)

                            isSkipped && centerProgress > 0.5f ->
                                Color(0xFFFF8A33)

                            centerProgress > 0.5f ->
                                JourneyGold

                            else ->
                                Color.White.copy(alpha = 0.12f)
                        },
                    shape = RoundedCornerShape(20.dp)
                )
        )

        if (isWatched || completionStage == CompletionStage.COMPLETED) {
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
        if (isSkipped && !isWatched) {
            Text(
                text = "»",
                color =
                    if (centerProgress > 0.5f) {
                        Color(0xFFFF8A33)
                    } else {
                        Color.Gray
                    },
                fontSize = 55.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
        if (movie.releaseStatus == "comingSoon") {
            Text(
                text = "COMING SOON",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JourneyGold)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun JourneyEndCard(
    universe: Universe,
    poster: String,
    journeyComplete: Boolean,
    centerProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = 0.68f + (0.32f * centerProgress)

    Box(
        modifier = modifier
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
        ArtworkImage(
            source = poster,
            placeholder = R.drawable.placeholder_poster,
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
