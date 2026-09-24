//
//  AppDelegate.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-22.
//

import UIKit
import UserNotifications
import FirebaseMessaging

final class AppDelegate: NSObject,
                         UIApplicationDelegate,
                         MessagingDelegate,
                         UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions:
            [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {

        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self

        return true
    }
    
    func requestNotificationPermission() {

        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, error in

            if let error {
                print(
                    "❌ Notification permission error:",
                    error.localizedDescription
                )
                return
            }

            guard granted else {
                print("🔕 Notification permission denied")
                return
            }

            DispatchQueue.main.async {
                UIApplication.shared
                    .registerForRemoteNotifications()
            }
        }
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken

        let token = deviceToken
            .map { String(format: "%02.2hhx", $0) }
            .joined()

        print("🍎 APNs token:", token)
    }

    func messaging(
        _ messaging: Messaging,
        didReceiveRegistrationToken fcmToken: String?
    ) {
        guard let fcmToken else { return }

        print("🔥 FCM token:", fcmToken)

        NotificationTokenService.storeAndSync(
            fcmToken
        )
    }
}
