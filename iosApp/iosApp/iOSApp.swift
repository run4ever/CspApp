import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // Initialise Firebase au démarrage à partir de GoogleService-Info.plist.
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}