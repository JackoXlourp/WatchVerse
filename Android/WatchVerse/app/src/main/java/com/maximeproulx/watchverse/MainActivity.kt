package com.maximeproulx.watchverse

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            android.util.Log.d(
                "WatchVerseNotifications",
                if (granted) "Notification permission granted."
                else "Notification permission not granted."
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = android.net.Uri.parse(
                "android.resource://$packageName/${R.raw.watchverse}"
            )

            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                "new_universes_watchverse",
                "New universes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(soundUri, audioAttributes)
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(NotificationTokenService::storeAndSync)
            .addOnFailureListener { error ->
                android.util.Log.w(
                    "WatchVerseNotifications",
                    "Unable to obtain the current FCM token.",
                    error
                )
            }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val catalogStore = androidx.compose.runtime.remember {
                CatalogStore(applicationContext)
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
                val sessionUID = if (signedIn) AuthenticationService.currentUser()?.uid else null
                var currentUser by androidx.compose.runtime.remember(sessionUID) {
                    androidx.compose.runtime.mutableStateOf<WatchVerseUser?>(null)
                }
                var userLoaded by androidx.compose.runtime.remember(sessionUID) {
                    androidx.compose.runtime.mutableStateOf(false)
                }
                var profileLoadAttempt by androidx.compose.runtime.remember(sessionUID) {
                    mutableIntStateOf(0)
                }
                var googleSignInMessage by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }
                var selectingUniverse by androidx.compose.runtime.remember(sessionUID) {
                    androidx.compose.runtime.mutableStateOf(false)
                }

                androidx.compose.runtime.LaunchedEffect(sessionUID, profileLoadAttempt) {
                    if (sessionUID != null) {
                        userLoaded = false
                        AuthenticationService.loadCurrentUser { user ->
                            if (AuthenticationService.currentUser()?.uid == sessionUID) {
                                currentUser = user
                                userLoaded = true
                            }
                        }
                    }
                }

                androidx.compose.runtime.LaunchedEffect(sessionUID, userLoaded) {
                    if (sessionUID != null && userLoaded) {
                        currentUser?.let { user ->
                            catalogStore.loadInitialContent(user.currentUniverseID)
                        }
                    }
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
                    if (userLoaded) {
                        if (currentUser == null) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .padding(24.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.Text(
                                    text = "Unable to load your account.",
                                    color = Color.White
                                )
                                androidx.compose.material3.Button(
                                    onClick = { profileLoadAttempt++ }
                                ) {
                                    androidx.compose.material3.Text("Try Again")
                                }
                            }
                        } else if (currentUser?.displayName.isNullOrBlank()) {

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

                            if (
                                currentUser!!.currentUniverseID == null &&
                                !catalogStore.isLoading
                            ) {
                                UniverseSelectionScreen(
                                    availableUniverses = catalogStore.availableUniverses,
                                    onUniverseClick = { universe ->
                                        if (!selectingUniverse) {
                                            selectingUniverse = true
                                            val prepared = catalogStore
                                                .prepareAvailableUniverse(universe.id)
                                            if (prepared == null) {
                                                selectingUniverse = false
                                                android.widget.Toast.makeText(
                                                    this@MainActivity,
                                                    "Unable to load this universe.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                AuthenticationService.updateCurrentUniverseID(
                                                    universe.id
                                                ) { success ->
                                                    selectingUniverse = false
                                                    if (success) {
                                                        catalogStore.activateUniverse(prepared)
                                                        currentUser = currentUser?.copy(
                                                            currentUniverseID = universe.id
                                                        )
                                                        catalogStore.preloadArtworkInBackground(prepared)
                                                    } else {
                                                        android.widget.Toast.makeText(
                                                            this@MainActivity,
                                                            "Unable to save your current universe.",
                                                            android.widget.Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            } else if (
                                activeUniverse?.id == currentUser!!.currentUniverseID &&
                                !catalogStore.isLoading
                            ) {
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
                            } else if (!catalogStore.isLoading) {
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

                    if (
                        showLaunchArtwork ||
                        (signedIn && !userLoaded) ||
                        (signedIn && currentUser != null && catalogStore.isLoading)
                    ) {
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
