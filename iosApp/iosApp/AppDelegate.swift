//
//  AppDelegate.swift
//  iosApp
//

import UIKit
import AVFAudio
import Contacts
import ComposeApp
import EventKit

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        DIiOSKt.doInitKoinIos {
            let iosPermissionManager = IOSPermissionManager(
                systemPermissionHandlerLocator:
                    SystemPermissionHandlerLocator()
            )
            return ["PermissionManagerProxy": iosPermissionManager]
        }
        return true
    }
}
