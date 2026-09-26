package com.maximeproulx.watchverse

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray

private val SearchGold = Color(0xFFDEBD47)

@Composable
fun SearchScreen(
    universes: List<Universe>,
    visibleBadges: List<Badge>,
    isActivatingUniverse: Boolean,
    onUniverseClick: (Universe, String?) -> Unit,
    onBadgeClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("watchverse_search", android.content.Context.MODE_PRIVATE)
    }
    var recentSearches by remember(preferences) {
        mutableStateOf(
            runCatching {
                val saved = JSONArray(preferences.getString("recentSearches", "[]"))
                List(saved.length()) { index -> saved.getString(index) }
            }.getOrDefault(emptyList())
        )
    }
    var query by remember { mutableStateOf("") }
    var submittedQuery by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf(emptyList<SearchSuggestion>()) }
    var submittedResults by remember { mutableStateOf(emptyList<SearchResultGroup>()) }
    var isLoadingResults by remember { mutableStateOf(false) }
    val trimmedQuery = query.trim()
    val showingSubmittedResults = submittedQuery == trimmedQuery && trimmedQuery.isNotEmpty()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = showingSubmittedResults) {
        submittedQuery = null
        submittedResults = emptyList()
        isLoadingResults = false
    }

    fun submitSearch(value: String) {
        val submitted = value.trim()
        if (submitted.isEmpty()) return
        query = submitted
        focusManager.clearFocus()
        keyboard?.hide()
        recentSearches = (listOf(submitted) + recentSearches.filterNot {
            it.equals(submitted, ignoreCase = true)
        }).take(10)
        preferences.edit().putString("recentSearches", JSONArray(recentSearches).toString()).apply()
        if (submittedQuery != submitted) {
            submittedResults = emptyList()
            isLoadingResults = true
            submittedQuery = submitted
        }
        suggestions = emptyList()
    }

    LaunchedEffect(trimmedQuery, showingSubmittedResults, universes, visibleBadges) {
        if (trimmedQuery.isEmpty() || showingSubmittedResults) {
            suggestions = emptyList()
        } else {
            suggestions = emptyList()
            delay(150)
            val matches = withContext(Dispatchers.Default) {
                SearchEngine.suggestions(trimmedQuery, universes, visibleBadges)
            }
            if (query.trim() == trimmedQuery && submittedQuery != trimmedQuery) {
                suggestions = matches
            }
        }
    }

    LaunchedEffect(submittedQuery, universes, visibleBadges) {
        val submitted = submittedQuery ?: return@LaunchedEffect
        val results = withContext(Dispatchers.Default) {
            SearchEngine.results(submitted, universes, visibleBadges)
        }
        if (submittedQuery == submitted && query.trim() == submitted) {
            submittedResults = results
            isLoadingResults = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.appbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(if (showingSubmittedResults) 62.dp else 95.dp))
            if (!showingSubmittedResults) {
                Text(
                    text = "WatchVerse",
                    color = SearchGold,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = { value ->
                    query = value
                    if (submittedQuery != value.trim()) submittedQuery = null
                    if (value.isBlank()) {
                        suggestions = emptyList()
                        submittedResults = emptyList()
                    }
                },
                placeholder = { Text("Search WatchVerse") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_close),
                            contentDescription = "Clear search",
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp).clickable {
                                query = ""
                                submittedQuery = null
                                submittedResults = emptyList()
                                suggestions = emptyList()
                            }
                        )
                    }
                } else null,
                singleLine = true,
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitSearch(query) }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (trimmedQuery.isEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Recent Searches", color = SearchGold,
                                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (recentSearches.isNotEmpty()) {
                                TextButton(onClick = {
                                    recentSearches = emptyList()
                                    preferences.edit().putString("recentSearches", "[]").apply()
                                }) { Text("Clear", color = SearchGold) }
                            }
                        }
                    }
                    if (recentSearches.isEmpty()) {
                        item { Text("No recent searches yet.", color = Color.Gray) }
                    } else {
                        items(recentSearches, key = { it }) { recent ->
                            Text(
                                text = "◷  $recent",
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth().clickable { submitSearch(recent) }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                } else if (!showingSubmittedResults) {
                    item {
                        Text("Best Matches", color = SearchGold, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                    }
                    if (suggestions.isEmpty()) {
                        item { Text("No matches yet.", color = Color.Gray) }
                    } else {
                        items(suggestions, key = { it.id }) { suggestion ->
                            SearchResultRow(
                                source = suggestion.content?.poster ?: suggestion.badge?.artwork
                                    ?: suggestion.universe.poster,
                                title = suggestion.content?.title ?: suggestion.badge?.title
                                    ?: suggestion.universe.fullTitle,
                                subtitle = when {
                                    suggestion.badge != null -> "Badge · ${suggestion.universe.fullTitle}"
                                    suggestion.content != null -> suggestion.universe.fullTitle
                                    else -> "Universe"
                                },
                                kind = when {
                                    suggestion.badge != null -> SearchRowKind.BADGE
                                    suggestion.content != null -> SearchRowKind.CONTENT
                                    else -> SearchRowKind.UNIVERSE
                                },
                                enabled = !isActivatingUniverse,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                    if (suggestion.badge != null) onBadgeClick(suggestion.badge.id)
                                    else onUniverseClick(suggestion.universe, suggestion.content?.id)
                                }
                            )
                        }
                    }
                } else if (isLoadingResults) {
                    item { CircularProgressIndicator(color = SearchGold) }
                } else if (submittedResults.isEmpty()) {
                    item { Text("No results for \"$trimmedQuery\".", color = Color.Gray) }
                } else {
                    submittedResults.forEach { group ->
                        item(key = "universe-${group.universe.id}") {
                            if (group.universe.id == "watchverse") {
                                Text("WATCHVERSE", color = SearchGold,
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            } else {
                                SearchResultRow(
                                    source = group.universe.poster,
                                    title = group.universe.fullTitle,
                                    subtitle = null,
                                    kind = SearchRowKind.UNIVERSE,
                                    enabled = !isActivatingUniverse,
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboard?.hide()
                                        onUniverseClick(group.universe, null)
                                    }
                                )
                            }
                        }
                        items(group.content, key = { "content-${group.universe.id}-${it.id}" }) { movie ->
                            SearchResultRow(
                                source = movie.poster,
                                title = movie.title,
                                subtitle = null,
                                kind = SearchRowKind.CONTENT,
                                enabled = !isActivatingUniverse,
                                indented = true,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                    onUniverseClick(group.universe, movie.id)
                                }
                            )
                        }
                        items(group.badges, key = { "badge-${it.id}" }) { badge ->
                            SearchResultRow(
                                source = badge.artwork,
                                title = badge.title,
                                subtitle = null,
                                kind = SearchRowKind.BADGE,
                                enabled = !isActivatingUniverse,
                                indented = true,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                    onBadgeClick(badge.id)
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(150.dp)) }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding()
                .padding(top = 16.dp, end = 20.dp).size(42.dp)
                .clip(CircleShape).background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
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
    }
}

private enum class SearchRowKind { UNIVERSE, CONTENT, BADGE }

@Composable
private fun SearchResultRow(
    source: String,
    title: String,
    subtitle: String?,
    kind: SearchRowKind,
    enabled: Boolean,
    onClick: () -> Unit,
    indented: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = if (indented) 24.dp else 0.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val width = if (kind == SearchRowKind.UNIVERSE) 60.dp else 45.dp
        val height = when (kind) {
            SearchRowKind.UNIVERSE -> 60.dp
            SearchRowKind.CONTENT -> 68.dp
            SearchRowKind.BADGE -> 45.dp
        }
        ArtworkImage(
            source = source,
            contentDescription = null,
            modifier = Modifier.size(width, height).clip(RoundedCornerShape(8.dp)),
            contentScale = if (kind == SearchRowKind.BADGE) ContentScale.Fit else ContentScale.Crop,
            placeholder = if (kind == SearchRowKind.CONTENT) R.drawable.placeholder_movie
                else R.drawable.placeholder_poster
        )
        Column {
            Text(title, color = Color.White,
                fontWeight = if (kind == SearchRowKind.UNIVERSE) FontWeight.Bold else FontWeight.Normal)
            if (subtitle != null) Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
