package com.maximeproulx.watchverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.maximeproulx.watchverse.ui.theme.WatchVerseTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val catalogStore = androidx.compose.runtime.remember {
                CatalogStore(applicationContext)
            }

            androidx.compose.runtime.LaunchedEffect(catalogStore) {
                catalogStore.loadInitialContent(universeID = "mcu")
            }

            WatchVerseTheme {
                var showLaunchArtwork by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(true)
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    delay(700)
                    showLaunchArtwork = false
                }

                Box(modifier = Modifier.fillMaxSize()) {

                var signedIn by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(
                        AuthenticationService.isSignedIn()
                    )
                }
                var googleSignInMessage by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }

                val googleSignInFallbackLauncher =
                    androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        AuthenticationService.handleGoogleSignInFallbackResult(
                            data = result.data,
                            onError = { message ->
                                googleSignInMessage = message
                            }
                        ) { success ->
                            if (success) {
                                googleSignInMessage = null
                                signedIn = true
                            }
                        }
                    }

                if (signedIn) {

                    var currentUser by androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf<WatchVerseUser?>(null)
                    }

                    var userLoaded by androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(false)
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        AuthenticationService.loadCurrentUser { user ->
                            currentUser = user
                            userLoaded = true
                        }
                    }

                    if (userLoaded) {

                        if (currentUser?.displayName.isNullOrBlank()) {

                            NameSetupScreen(
                                onContinue = { name ->
                                    AuthenticationService.updateDisplayName(name) { success ->
                                        if (success) {
                                            currentUser = currentUser?.copy(
                                                displayName = name
                                            )
                                        }
                                    }
                                }
                            )

                        } else {

                            val activeUniverse = catalogStore.activeUniverse

                            if (activeUniverse != null) {
                                MainTabScreen(
                                    catalogStore = catalogStore,
                                    currentUser = currentUser!!,
                                    onCurrentUserChanged = { user ->
                                        currentUser = user
                                    },
                                    onSignedOut = {
                                        currentUser = null
                                        signedIn = false
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.Text(
                                        text = catalogStore.errorMessage
                                            ?: "Unable to load the WatchVerse catalog.",
                                        color = Color.White,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    AuthenticationScreen(
                        googleSignInMessage = googleSignInMessage,
                        onAppleSignInClick = {
                            AuthenticationService.signInWithApple(
                                activity = this@MainActivity
                            ) { success ->
                                if (success) {
                                    signedIn = true
                                }
                            }
                        },
                        onGoogleSignInClick = {
                            googleSignInMessage = null
                            AuthenticationService.signInWithGoogle(
                                activity = this@MainActivity,
                                launchFallback = { intent ->
                                    googleSignInFallbackLauncher.launch(intent)
                                },
                                onError = { message ->
                                    googleSignInMessage = message
                                }
                            ) { success ->
                                if (success) {
                                    googleSignInMessage = null
                                    signedIn = true
                                }
                            }
                        }
                    )
                }

                    if (showLaunchArtwork || catalogStore.isLoading) {
                        Image(
                            painter = painterResource(R.drawable.wv_launch),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
