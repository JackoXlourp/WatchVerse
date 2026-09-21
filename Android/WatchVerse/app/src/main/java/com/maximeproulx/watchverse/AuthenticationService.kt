package com.maximeproulx.watchverse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

object AuthenticationService {

    private const val currentUserSchemaVersion = 2L
    private const val legacyFilterUniverseID = "mcu"

    sealed class DeleteAccountResult {
        data object Success : DeleteAccountResult()
        data class Failure(val message: String) : DeleteAccountResult()
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }

    fun createUserDocumentIfNeeded(
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        val userRef = db
            .collection("users")
            .document(user.uid)

        userRef.get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val userData = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to (user.displayName ?: ""),
                    "email" to (user.email ?: ""),
                    "joinedDate" to System.currentTimeMillis(),
                    "isFounder" to false,
                    "showReleaseYears" to true,
                    "notifyNewUniverses" to true,
                    "selectedUniverseFilters" to emptyMap<String, List<String>>(),
                    "journeyPositions" to emptyMap<String, String>(),
                    "unlockedBadges" to emptyList<String>(),
                    "shownBadgePopups" to emptyList<String>(),
                    "watchedMovies" to emptyList<String>(),
                    "skippedMovies" to emptyList<String>(),
                    "schemaVersion" to currentUserSchemaVersion
                )

                userRef
                    .set(userData)
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun loadCurrentUser(
        onComplete: (WatchVerseUser?) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(null)
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onComplete(null)
                    return@addOnSuccessListener
                }

                onComplete(userFromDocument(document))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
    fun updateShowReleaseYears(
        value: Boolean,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update(
                "showReleaseYears",
                value
            )
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun updateNotifyNewUniverses(
        value: Boolean,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update(
                "notifyNewUniverses",
                value
            )
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun updateSelectedUniverseFilters(
        filtersByUniverse: Map<String, List<String>>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update(
                "selectedUniverseFilters",
                filtersByUniverse
            )
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    private fun userFromDocument(
        document: com.google.firebase.firestore.DocumentSnapshot
    ): WatchVerseUser {
        val filters = decodeUniverseFilters(
            rawFilters = document.get("selectedUniverseFilters")
        )

        return WatchVerseUser(
            uid = document.getString("uid") ?: document.id,
            displayName = document.getString("displayName") ?: "",
            email = document.getString("email") ?: "",
            joinedDate = document.getLong("joinedDate") ?: 0L,
            isFounder = document.getBoolean("isFounder") ?: false,
            showReleaseYears = document.getBoolean("showReleaseYears") ?: true,
            notifyNewUniverses = document.getBoolean("notifyNewUniverses") ?: true,
            selectedUniverseFilters = filters,
            journeyPositions = decodeStringMap(document.get("journeyPositions")),
            unlockedBadges = decodeStringList(document.get("unlockedBadges")),
            shownBadgePopups = decodeStringList(document.get("shownBadgePopups")),
            watchedMovies = decodeStringList(document.get("watchedMovies")),
            skippedMovies = decodeStringList(document.get("skippedMovies")),
            schemaVersion = document.getLong("schemaVersion") ?: 1L,
            cloudKitMigrationVersion = document.getLong("cloudKitMigrationVersion"),
            legacyCloudKitRecordID = document.getString("legacyCloudKitRecordID")
        )
    }

    private fun decodeUniverseFilters(
        rawFilters: Any?
    ): Map<String, List<String>> {
        val perUniverse = rawFilters as? Map<*, *>
        if (perUniverse != null) {
            return perUniverse.mapNotNull { (universeID, filters) ->
                val id = universeID as? String ?: return@mapNotNull null
                id to decodeStringList(filters)
            }.toMap()
        }

        val legacyFilters = decodeStringList(rawFilters)
        return if (legacyFilters.isEmpty()) {
            emptyMap()
        } else {
            mapOf(legacyFilterUniverseID to legacyFilters)
        }
    }

    private fun decodeStringMap(rawMap: Any?): Map<String, String> {
        return (rawMap as? Map<*, *>)
            ?.mapNotNull { (key, value) ->
                val stringKey = key as? String ?: return@mapNotNull null
                val stringValue = value as? String ?: return@mapNotNull null
                stringKey to stringValue
            }
            ?.toMap()
            ?: emptyMap()
    }

    private fun decodeStringList(rawList: Any?): List<String> {
        return (rawList as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }
    fun updateJourneyPositions(
        positions: Map<String, String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update("journeyPositions", positions)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun updateWatchedMovies(
        watchedMovies: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update("watchedMovies", watchedMovies)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun updateSkippedMovies(
        skippedMovies: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update("skippedMovies", skippedMovies)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun updateUnlockedBadges(
        unlockedBadges: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update("unlockedBadges", unlockedBadges)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun updateShownBadgePopups(
        shownBadgePopups: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update("shownBadgePopups", shownBadgePopups)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun updateDisplayName(
        name: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update(
                "displayName",
                name
            )
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun resetUniverseProgress(
        movieIds: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        val userRef = db
            .collection("users")
            .document(user.uid)

        userRef.get()
            .addOnSuccessListener { document ->

                val currentWatched =
                    (document.get("watchedMovies") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()

                val currentSkipped =
                    (document.get("skippedMovies") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()

                val updatedWatched =
                    currentWatched.filterNot { movieIds.contains(it) }

                val updatedSkipped =
                    currentSkipped.filterNot { movieIds.contains(it) }

                userRef.update(
                    mapOf(
                        "watchedMovies" to updatedWatched,
                        "skippedMovies" to updatedSkipped
                    )
                )
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun resetAllProgress(
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "watchedMovies" to emptyList<String>(),
                    "skippedMovies" to emptyList<String>()
                )
            )
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteCurrentUser(
        activity: androidx.activity.ComponentActivity,
        onComplete: (DeleteAccountResult) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onComplete(
                DeleteAccountResult.Failure("No signed-in account was found.")
            )
            return
        }

        val providerIds = user.providerData.map { it.providerId }

        when {
            providerIds.contains("google.com") -> {
                reauthenticateGoogle(activity, user) { success, message ->
                    if (success) {
                        deleteFirestoreAndAuthUser(user, onComplete)
                    } else {
                        onComplete(DeleteAccountResult.Failure(message))
                    }
                }
            }

            providerIds.contains("apple.com") -> {
                reauthenticateApple(activity, user) { success, message ->
                    if (success) {
                        deleteFirestoreAndAuthUser(user, onComplete)
                    } else {
                        onComplete(DeleteAccountResult.Failure(message))
                    }
                }
            }

            else -> {
                onComplete(
                    DeleteAccountResult.Failure(
                        "This sign-in provider cannot be reauthenticated. Log out, sign in again, and retry."
                    )
                )
            }
        }
    }

    private fun reauthenticateGoogle(
        activity: androidx.activity.ComponentActivity,
        user: FirebaseUser,
        onComplete: (Boolean, String) -> Unit
    ) {
        val credentialManager =
            androidx.credentials.CredentialManager.create(activity)

        val googleIdOption =
            com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    activity.getString(
                        activity.resources.getIdentifier(
                            "default_web_client_id",
                            "string",
                            activity.packageName
                        )
                    )
                )
                .setAutoSelectEnabled(false)
                .build()

        val request =
            androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

        activity.lifecycleScope.launch {
            try {
                val credential = credentialManager.getCredential(
                    context = activity,
                    request = request
                ).credential

                if (
                    credential !is androidx.credentials.CustomCredential ||
                    credential.type !=
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    onComplete(false, "Google reauthentication was cancelled or returned an invalid credential.")
                    return@launch
                }

                val googleCredential =
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                        .createFrom(credential.data)

                val firebaseCredential =
                    com.google.firebase.auth.GoogleAuthProvider.getCredential(
                        googleCredential.idToken,
                        null
                    )

                user.reauthenticate(firebaseCredential)
                    .addOnSuccessListener {
                        onComplete(true, "")
                    }
                    .addOnFailureListener { error ->
                        onComplete(
                            false,
                            "Google reauthentication failed: ${error.localizedMessage ?: "Unknown error"}"
                        )
                    }
            } catch (error: Exception) {
                onComplete(
                    false,
                    "Google reauthentication failed: ${error.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    private fun reauthenticateApple(
        activity: androidx.activity.ComponentActivity,
        user: FirebaseUser,
        onComplete: (Boolean, String) -> Unit
    ) {
        val provider = com.google.firebase.auth.OAuthProvider
            .newBuilder("apple.com")

        provider.scopes = listOf("email", "name")

        user.startActivityForReauthenticateWithProvider(
            activity,
            provider.build()
        )
            .addOnSuccessListener {
                onComplete(true, "")
            }
            .addOnFailureListener { error ->
                onComplete(
                    false,
                    "Apple reauthentication failed: ${error.localizedMessage ?: "Unknown error"}"
                )
            }
    }

    private fun deleteFirestoreAndAuthUser(
        user: FirebaseUser,
        onComplete: (DeleteAccountResult) -> Unit
    ) {
        val uid = user.uid

        db.collection("users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        onComplete(DeleteAccountResult.Success)
                    }
                    .addOnFailureListener { error ->
                        onComplete(
                            DeleteAccountResult.Failure(
                                "The account data was removed, but Firebase Authentication deletion failed: " +
                                        (error.localizedMessage ?: "Unknown error")
                            )
                        )
                    }
            }
            .addOnFailureListener { error ->
                onComplete(
                    DeleteAccountResult.Failure(
                        "Firestore account deletion failed: ${error.localizedMessage ?: "Unknown error"}"
                    )
                )
            }
    }
    fun signInWithGoogle(
        activity: androidx.activity.ComponentActivity,
        launchFallback: (android.content.Intent) -> Unit,
        onError: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        val credentialManager =
            androidx.credentials.CredentialManager.create(activity)

        val signInWithGoogleOption =
            com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption.Builder(
                activity.getString(R.string.default_web_client_id)
            )
                .build()

        val request =
            androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

        activity.lifecycleScope.launch {
            try {
                val mutableContext = android.content.MutableContextWrapper(activity)

                val result =
                    credentialManager.getCredential(
                        context = mutableContext,
                        request = request
                    )

                val credential = result.credential

                if (
                    credential is androidx.credentials.CustomCredential &&
                    credential.type ==
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                            .createFrom(credential.data)

                    if (googleCredential.idToken.isBlank()) {
                        android.util.Log.e(
                            "WatchVerseAuth",
                            "Credential Manager returned an empty Google ID token."
                        )
                        onError("Google sign-in failed (TOKEN)")
                        onComplete(false)
                    } else {
                        signInToFirebaseWithGoogleIdToken(
                            idToken = googleCredential.idToken,
                            onError = onError,
                            onComplete = onComplete
                        )
                    }
                } else {
                    android.util.Log.e(
                        "WatchVerseAuth",
                        "Credential Manager returned unsupported credential type ${credential::class.java.name}."
                    )
                    onError("Google sign-in failed (CM-TYPE)")
                    onComplete(false)
                }

            } catch (error: androidx.credentials.exceptions.NoCredentialException) {
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Google Credential Manager returned ${error::class.java.name}",
                    error
                )

                onError("Google sign-in failed (CM-NONE)")
                onComplete(false)
            } catch (error: androidx.credentials.exceptions.GetCredentialCancellationException) {
                val message = error.message.orEmpty()
                val isAccountReauthStatus16 =
                    message.contains("[16]") &&
                            message.contains("Account reauth failed", ignoreCase = true)

                if (isAccountReauthStatus16) {
                    android.util.Log.w(
                        "WatchVerseAuth",
                        "Credential Manager account reauth failed with status 16; launching legacy Google Sign-In fallback.",
                        error
                    )

                    try {
                        val options =
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                            )
                                .requestEmail()
                                .requestIdToken(activity.getString(R.string.default_web_client_id))
                                .build()

                        val client =
                            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                                activity,
                                options
                            )

                        launchFallback(client.signInIntent)
                    } catch (fallbackError: Exception) {
                        android.util.Log.e(
                            "WatchVerseAuth",
                            "Unable to launch legacy Google Sign-In fallback: ${fallbackError::class.java.name}",
                            fallbackError
                        )
                        onError("Google sign-in failed (CM-16)")
                        onComplete(false)
                    }
                } else {
                    android.util.Log.i(
                        "WatchVerseAuth",
                        "Google sign-in was cancelled: ${error::class.java.name}",
                        error
                    )
                    onError("Google sign-in cancelled (CM-CANCEL)")
                    onComplete(false)
                }
            } catch (error: com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException) {
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Credential Manager Google ID token parsing failed: ${error::class.java.name}",
                    error
                )
                onError("Google sign-in failed (CM-PARSE)")
                onComplete(false)
            } catch (error: androidx.credentials.exceptions.GetCredentialException) {
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Google Credential Manager failed with ${error::class.java.name}",
                    error
                )
                onError("Google sign-in failed (CM-OTHER)")
                onComplete(false)
            } catch (error: Exception) {
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Unexpected Google sign-in failure: ${error::class.java.name}",
                    error
                )
                onError("Google sign-in failed (UNEXPECTED)")
                onComplete(false)
            }
        }
    }

    fun handleGoogleSignInFallbackResult(
        data: android.content.Intent?,
        onError: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        val accountTask =
            com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = accountTask.getResult(
                com.google.android.gms.common.api.ApiException::class.java
            )
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Legacy Google Sign-In fallback returned no ID token."
                )
                onError("Google sign-in failed (TOKEN)")
                onComplete(false)
                return
            }

            signInToFirebaseWithGoogleIdToken(
                idToken = idToken,
                onError = onError,
                onComplete = onComplete
            )
        } catch (error: com.google.android.gms.common.api.ApiException) {
            android.util.Log.e(
                "WatchVerseAuth",
                "Legacy Google Sign-In fallback failed with status ${error.statusCode}",
                error
            )
            if (
                error.statusCode ==
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED
            ) {
                onError("Google sign-in cancelled (GSI-${error.statusCode})")
            } else {
                onError("Google sign-in failed (GSI-${error.statusCode})")
            }
            onComplete(false)
        } catch (error: Exception) {
            android.util.Log.e(
                "WatchVerseAuth",
                "Unexpected legacy Google Sign-In fallback failure: ${error::class.java.name}",
                error
            )
            onError("Google sign-in failed (GSI-UNEXPECTED)")
            onComplete(false)
        }
    }

    private fun signInToFirebaseWithGoogleIdToken(
        idToken: String,
        onError: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        val firebaseCredential =
            com.google.firebase.auth.GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                createUserDocumentIfNeeded { success ->
                    if (!success) {
                        android.util.Log.e(
                            "WatchVerseAuth",
                            "Firebase sign-in succeeded, but the WatchVerse user document could not be loaded or created."
                        )
                        onError("Google sign-in failed (PROFILE)")
                    }
                    onComplete(success)
                }
            }
            .addOnFailureListener { error ->
                android.util.Log.e(
                    "WatchVerseAuth",
                    "Firebase Google credential exchange failed: ${error::class.java.name}",
                    error
                )
                onError("Google sign-in failed (FIREBASE)")
                onComplete(false)
            }
    }

    fun signInWithApple(
        activity: androidx.activity.ComponentActivity,
        onComplete: (Boolean) -> Unit
    ) {
        val provider =
            com.google.firebase.auth.OAuthProvider
                .newBuilder("apple.com")

        provider.scopes = listOf(
            "email",
            "name"
        )

        val pendingResult = auth.pendingAuthResult

        if (pendingResult != null) {
            pendingResult
                .addOnSuccessListener {
                    createUserDocumentIfNeeded {
                            success ->
                        onComplete(success)
                    }
                }
                .addOnFailureListener {
                    onComplete(false)
                }

            return
        }

        auth.startActivityForSignInWithProvider(
            activity,
            provider.build()
        )
            .addOnSuccessListener {
                createUserDocumentIfNeeded {
                        success ->
                    onComplete(success)
                }
            }
            .addOnFailureListener { error ->
                android.widget.Toast.makeText(
                    activity,
                    "Apple sign-in error: ${error.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                onComplete(false)
            }
    }
}
