import Foundation
import Combine
import UserNotifications

@MainActor
final class ReminderSettings: ObservableObject {
    static let shared = ReminderSettings()

    @Published private(set) var enabled: Bool
    @Published private(set) var hour: Int
    @Published private(set) var minute: Int
    @Published var error: String?

    private let defaults = UserDefaults.standard
    private let identifier = "gould.practice-reminder"

    private init() {
        enabled = defaults.bool(forKey: "reminder.enabled")
        hour = min(23, max(0, defaults.object(forKey: "reminder.hour") as? Int ?? 18))
        minute = min(59, max(0, defaults.object(forKey: "reminder.minute") as? Int ?? 0))
    }

    func update(enabled requested: Bool, hour: Int? = nil, minute: Int? = nil) async {
        let nextHour = min(23, max(0, hour ?? self.hour))
        let nextMinute = min(59, max(0, minute ?? self.minute))
        if requested {
            do {
                let center = UNUserNotificationCenter.current()
                let settings = await center.notificationSettings()
                var granted = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
                if settings.authorizationStatus == .notDetermined {
                    granted = try await center.requestAuthorization(options: [.alert, .sound]).contains(.alert)
                }
                guard granted else {
                    error = tr("settings.notificationsDenied")
                    enabled = false
                    defaults.set(false, forKey: "reminder.enabled")
                    return
                }
            } catch {
                self.error = tr("settings.reminderFailed")
                return
            }
        }
        self.hour = nextHour
        self.minute = nextMinute
        enabled = requested
        defaults.set(requested, forKey: "reminder.enabled")
        defaults.set(nextHour, forKey: "reminder.hour")
        defaults.set(nextMinute, forKey: "reminder.minute")
        error = nil

        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: [identifier])
        guard requested else { return }
        let content = UNMutableNotificationContent()
        content.title = tr("reminder.title")
        content.body = tr("reminder.body")
        content.sound = .default
        var components = DateComponents()
        components.hour = nextHour
        components.minute = nextMinute
        let request = UNNotificationRequest(identifier: identifier, content: content,
                                            trigger: UNCalendarNotificationTrigger(dateMatching: components, repeats: true))
        do { try await center.add(request) }
        catch {
            self.error = tr("settings.reminderFailed")
            enabled = false
            defaults.set(false, forKey: "reminder.enabled")
        }
    }

    func restoreSchedule() async {
        guard enabled else { return }
        let center = UNUserNotificationCenter.current()
        let requests = await center.pendingNotificationRequests()
        if !requests.contains(where: { $0.identifier == identifier }) {
            await update(enabled: true)
        }
    }
}
