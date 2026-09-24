package com.maximeproulx.watchverse

import com.google.firebase.messaging.FirebaseMessagingService

class WatchVerseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        NotificationTokenService.storeAndSync(token)
    }
}
