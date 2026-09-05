package com.maximeproulx.watchverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.maximeproulx.watchverse.ui.theme.WatchVerseTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

                            MainTabScreen(
                                currentUser = currentUser!!,
                                onCurrentUserChanged = { user ->
                                    currentUser = user
                                },
                                onSignedOut = {
                                    currentUser = null
                                    signedIn = false
                                }
                            )
                        }
                    }
                } else {
                    AuthenticationScreen(
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
                            AuthenticationService.signInWithGoogle(
                                activity = this@MainActivity
                            ) { success ->
                                if (success) {
                                    signedIn = true
                                }
                            }
                        }
                    )
                }

                    if (showLaunchArtwork) {
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
