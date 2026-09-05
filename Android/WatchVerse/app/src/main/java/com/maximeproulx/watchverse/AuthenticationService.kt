package com.maximeproulx.watchverse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

object AuthenticationService {

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
                    "isFounder" to true,
                    "showReleaseYears" to true,
                    "notifyNewUniverses" to true,
                    "unlockedBadges" to emptyList<String>(),
                    "shownBadgePopups" to emptyList<String>(),
                    "watchedMovies" to emptyList<String>(),
                    "skippedMovies" to emptyList<String>()
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

                val watchVerseUser = document.toObject(
                    WatchVerseUser::class.java
                )

                val fixedUser = watchVerseUser?.copy(
                    isFounder = document.getBoolean("isFounder") ?: false,
                    joinedDate = document.getLong("joinedDate") ?: 0L
                )

                onComplete(fixedUser)
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
                .setFilterByAuthorizedAccounts(true)
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
        onComplete: (Boolean) -> Unit
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
                val result =
                    credentialManager.getCredential(
                        context = activity,
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

                    val firebaseCredential =
                        com.google.firebase.auth.GoogleAuthProvider.getCredential(
                            googleCredential.idToken,
                            null
                        )

                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            createUserDocumentIfNeeded {
                                onComplete(it)
                            }
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
                }

            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    activity,
                    "Google sign-in error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                onComplete(false)
            }
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
