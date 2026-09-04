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
    onSignedOut: () -> Unit = {}
) {

    var selectedTab by remember {
        mutableStateOf(WatchVerseTab.HOME)
    }
    var hideBottomBar by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (selectedTab) {

            WatchVerseTab.HOME -> {
                HomeScreen()
            }

            WatchVerseTab.JOURNEY -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                val activeUniverse = JSONLoader.loadActiveUniverse(context)

                JourneyScreen(
                    universe = activeUniverse,
                    onSignedOut = onSignedOut,
                    onFullScreenOverlayChanged = { hidden ->
                        hideBottomBar = hidden
                    }
                )
            }

            WatchVerseTab.BADGES -> {
                PlaceholderScreen("Badges")
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