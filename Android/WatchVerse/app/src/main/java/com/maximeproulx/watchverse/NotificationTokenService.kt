package com.maximeproulx.watchverse

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/** Keeps the device's latest FCM token across account changes, as on iOS. */
object NotificationTokenService {
    private const val TAG = "WatchVerseNotifications"
    private const val PREFERENCES = "watchverse_notifications"
    private const val TOKEN_KEY = "latestFCMToken"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun storedToken(): String? = FirebaseApp.getInstance()
        .applicationContext
        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(TOKEN_KEY, null)
        ?.takeIf(String::isNotBlank)

    fun storeAndSync(token: String) {
        if (token.isBlank()) return

        FirebaseApp.getInstance()
            .applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()

        syncStoredTokenIfPossible()
    }

    fun syncStoredTokenIfPossible() {
        val token = storedToken() ?: return
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener {
                Log.d(TAG, "FCM token synced to the signed-in profile.")
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "FCM token sync failed.", error)
            }
    }

    fun removeStoredTokenFromCurrentUser(onComplete: () -> Unit) {
        val token = storedToken()
        val uid = auth.currentUser?.uid
        if (token == null || uid == null) {
            onComplete()
            return
        }

        db.collection("users")
            .document(uid)
            .update("fcmTokens", FieldValue.arrayRemove(token))
            .addOnSuccessListener {
                Log.d(TAG, "FCM token removed from the signed-in profile.")
                onComplete()
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "FCM token removal failed before sign-out.", error)
                onComplete()
            }
    }
}
