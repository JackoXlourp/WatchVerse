package com.maximeproulx.watchverse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource

private val TabGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

private enum class WatchVerseTab {
    HOME,
    JOURNEY,
    BADGES
}

@Composable
fun MainTabScreen(
    currentUser: WatchVerseUser,
    onCurrentUserChanged: (WatchVerseUser) -> Unit,
    onSignedOut: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeUniverse = remember(context.applicationContext) {
        JSONLoader.loadActiveUniverse(context.applicationContext)
    }

    var selectedTab by remember {
        mutableStateOf(WatchVerseTab.HOME)
    }
    var hideBottomBar by remember {
        mutableStateOf(false)
    }
    var showSettings by remember {
        mutableStateOf(false)
    }
    var queuedBadgePopups by remember {
        mutableStateOf(emptyList<Badge>())
    }
    var popupBadge by remember {
        mutableStateOf<Badge?>(null)
    }
    var badgeGalleryScrollTargetID by remember {
        mutableStateOf<String?>(null)
    }

    fun queueBadgePopups(badges: List<Badge>) {
        val queuedIDs = queuedBadgePopups.map { badge -> badge.id }.toSet()
        val popupBadgeID = popupBadge?.id
        val badgesToQueue = badges.filter { badge ->
            !currentUser.shownBadgePopups.contains(badge.id) &&
                    badge.id != popupBadgeID &&
                    !queuedIDs.contains(badge.id)
        }

        if (badgesToQueue.isNotEmpty()) {
            queuedBadgePopups = queuedBadgePopups + badgesToQueue
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        currentUser.uid,
        currentUser.isFounder,
        currentUser.shownBadgePopups
    ) {
        if (
            currentUser.isFounder &&
            !currentUser.shownBadgePopups.contains("founder")
        ) {
            BadgeData.all
                .firstOrNull { badge -> badge.id == "founder" }
                ?.let { founderBadge ->
                    queueBadgePopups(listOf(founderBadge))
                }
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        queuedBadgePopups,
        popupBadge
    ) {
        if (popupBadge == null && queuedBadgePopups.isNotEmpty()) {
            val nextBadge = queuedBadgePopups.first()

            delay(500)

            queuedBadgePopups = queuedBadgePopups.drop(1)

            if (!currentUser.shownBadgePopups.contains(nextBadge.id)) {
                val updatedShownBadgePopups =
                    currentUser.shownBadgePopups + nextBadge.id

                popupBadge = nextBadge
                onCurrentUserChanged(
                    currentUser.copy(
                        shownBadgePopups = updatedShownBadgePopups
                    )
                )
                AuthenticationService.updateShownBadgePopups(
                    updatedShownBadgePopups
                )
            }
        }
    }

    fun openSettings() {
        showSettings = true
        hideBottomBar = true
    }

    fun closeSettings() {
        showSettings = false
        hideBottomBar = false
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (selectedTab) {

            WatchVerseTab.HOME -> {
                HomeScreen(
                    onContinueWatchingClick = {
                        selectedTab = WatchVerseTab.JOURNEY
                    },
                    onSettingsClick = ::openSettings
                )
            }

            WatchVerseTab.JOURNEY -> {
                JourneyScreen(
                    universe = activeUniverse,
                    currentUser = currentUser,
                    onCurrentUserChanged = onCurrentUserChanged,
                    onSettingsClick = ::openSettings,
                    onBadgesUnlocked = { badges ->
                        queueBadgePopups(badges)
                    },
                    onFullScreenOverlayChanged = { hidden ->
                        hideBottomBar = hidden
                    },
                )
            }

            WatchVerseTab.BADGES -> {
                BadgeGalleryScreen(
                    currentUser = currentUser,
                    scrollTargetBadgeID = badgeGalleryScrollTargetID,
                    onScrollTargetConsumed = {
                        badgeGalleryScrollTargetID = null
                    },
                    onSettingsClick = ::openSettings,
                    onFullScreenOverlayChanged = { hidden ->
                        hideBottomBar = hidden
                    }
                )
            }
        }

        if (!hideBottomBar) {
            WatchVerseBottomBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp
                    )
            )
        }
        if (showSettings) {
            SettingsScreen(
                universe = activeUniverse,
                showReleaseYears = currentUser.showReleaseYears,
                onShowReleaseYearsChanged = { value ->
                    val previousUser = currentUser
                    onCurrentUserChanged(
                        currentUser.copy(showReleaseYears = value)
                    )
                    AuthenticationService.updateShowReleaseYears(value) { success ->
                        if (!success) {
                            onCurrentUserChanged(previousUser)
                        }
                    }
                },
                notifyNewUniverses = currentUser.notifyNewUniverses,
                onNotifyNewUniversesChanged = { value ->
                    val previousUser = currentUser
                    onCurrentUserChanged(
                        currentUser.copy(notifyNewUniverses = value)
                    )
                    AuthenticationService.updateNotifyNewUniverses(value) { success ->
                        if (!success) {
                            onCurrentUserChanged(previousUser)
                        }
                    }
                },
                displayName = currentUser.displayName,
                accountSubtitle = currentUser.let { user ->
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
                },
                onResetUniverseProgress = {
                    val movieIds = activeUniverse.movies.map { it.id }.toSet()
                    AuthenticationService.resetUniverseProgress(
                        movieIds = movieIds.toList()
                    ) { success ->
                        if (success) {
                            onCurrentUserChanged(
                                currentUser.copy(
                                    watchedMovies = currentUser.watchedMovies.filterNot(movieIds::contains),
                                    skippedMovies = currentUser.skippedMovies.filterNot(movieIds::contains)
                                )
                            )
                        }
                    }
                },
                onResetAllProgress = {
                    AuthenticationService.resetAllProgress { success ->
                        if (success) {
                            onCurrentUserChanged(
                                currentUser.copy(
                                    watchedMovies = emptyList(),
                                    skippedMovies = emptyList()
                                )
                            )
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to reset all progress. Please try again.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                onLogout = {
                    AuthenticationService.signOut()
                    closeSettings()
                    onSignedOut()
                },
                onDeleteAccount = {
                    val activity = context as? androidx.activity.ComponentActivity

                    if (activity == null) {
                        android.widget.Toast.makeText(
                            context,
                            "Unable to start account verification.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        AuthenticationService.deleteCurrentUser(activity) { result ->
                            when (result) {
                                AuthenticationService.DeleteAccountResult.Success -> {
                                    AuthenticationService.signOut()
                                    closeSettings()
                                    onSignedOut()
                                }

                                is AuthenticationService.DeleteAccountResult.Failure -> {
                                    android.widget.Toast.makeText(
                                        context,
                                        result.message,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                },
                onClose = ::closeSettings
            )
        }

        popupBadge?.let { badge ->
            BadgeUnlockOverlay(
                badge = badge,
                onClose = {
                    popupBadge = null
                },
                onSeeBadge = {
                    popupBadge = null
                    badgeGalleryScrollTargetID = badge.id
                    selectedTab = WatchVerseTab.BADGES
                }
            )
        }
    }
}

@Composable
private fun WatchVerseBottomBar(
    selectedTab: WatchVerseTab,
    onTabSelected: (WatchVerseTab) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF242424))
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        TabItem(
            label = "Home",
            iconRes = R.drawable.ic_home,
            selected = selectedTab == WatchVerseTab.HOME,
            onClick = {
                onTabSelected(WatchVerseTab.HOME)
            },
            modifier = Modifier.weight(1f)
        )

        TabItem(
            label = "Journey",
            iconRes = R.drawable.ic_journey,
            selected = selectedTab == WatchVerseTab.JOURNEY,
            onClick = {
                onTabSelected(WatchVerseTab.JOURNEY)
            },
            modifier = Modifier.weight(1f)
        )

        TabItem(
            label = "Badges",
            iconRes = R.drawable.ic_badge,
            selected = selectedTab == WatchVerseTab.BADGES,
            onClick = {
                onTabSelected(WatchVerseTab.BADGES)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val backgroundColor =
        if (selected) {
            Color(0xFF3A3A3A)
        } else {
            Color.Transparent
        }

    val contentColor =
        if (selected) {
            TabGold
        } else {
            Color.White
        }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable {
                onClick()
            }
            .padding(
                vertical = 7.dp,
                horizontal = 4.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(contentColor)
        )

        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
