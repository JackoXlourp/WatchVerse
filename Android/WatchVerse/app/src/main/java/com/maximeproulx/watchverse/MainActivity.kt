package com.maximeproulx.watchverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.maximeproulx.watchverse.ui.theme.WatchVerseTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WatchVerseTheme {

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
                                onSignedOut = {
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
            }
        }
    }
}
