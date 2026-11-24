import Foundation
import XCTest
import ActivityDomain
@testable import Gould

final class ActivityStoreTests: XCTestCase {
    private func fileURL(_ name: String = UUID().uuidString + ".json") -> URL {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        try? FileManager.default.createDirectory(at: base, withIntermediateDirectories: true)
        return base.appendingPathComponent(name)
    }

    private func json(_ value: [String: Any], to url: URL) throws -> Data {
        let data = try JSONSerialization.data(withJSONObject: value)
        try data.write(to: url, options: .atomic)
        return data
    }

    func testKotlinFrameworkInteropSmokeCheck() {
        ActivityRewardsInteropCheck.compile()
    }

    @MainActor
    func testLegacyFileMigratesWithoutDroppingActivity() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        try json(["workouts": [], "goals": []], to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertNil(store.lastError)
        let migrated = try XCTUnwrap(Data(contentsOf: url))
        let object = try XCTUnwrap(JSONSerialization.jsonObject(with: migrated) as? [String: Any])
        XCTAssertNotNil(object["firstUseDay"])
        XCTAssertNotNil(object["weeks"])
        XCTAssertEqual((object["workouts"] as? [[String: Any]])?.count, 0)
    }

    @MainActor
    func testSemanticallyInvalidWeekPreservesOriginalFileUntilReset() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let today = EpochDay.of(Date())
        let monday = today - Int64(EpochDay.mondayIndex(today) - 1)
        let original = try json([
            "firstUseDay": today, "workouts": [], "goals": [], "weeks": [[
                "monday": monday, "zoneId": "", "beta": false,
            ]],
        ], to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertEqual(try Data(contentsOf: url), original)
        XCTAssertEqual(store.lastError, tr("activity.loadFailed"))
        store.resetAllData()
        XCTAssertNil(store.lastError)
        XCTAssertTrue(store.workouts.isEmpty)
    }

    @MainActor
    func testInvalidWorkoutIsPreservedAndNotPartiallyLoaded() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let original = try json([
            "workouts": [["epochDay": EpochDay.of(Date()), "durationMillis": 0,
                          "sourceId": "manual-invalid", "sourceRaw": "MANUAL_ENTRY"]],
            "goals": [],
        ], to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertEqual(try Data(contentsOf: url), original)
        XCTAssertTrue(store.workouts.isEmpty)
        XCTAssertEqual(store.lastError, tr("activity.loadFailed"))
    }

    @MainActor
    func testWorkoutWithReversedTimestampsIsPreservedAsInvalid() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let original = try json([
            "workouts": [["epochDay": EpochDay.of(Date()), "durationMillis": 60_000,
                          "sourceId": "practice:reversed", "sourceRaw": "PRACTICE_SESSION",
                          "startedAtMillis": 2_000, "completedAtMillis": 1_000]],
            "goals": [],
        ], to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertEqual(try Data(contentsOf: url), original)
        XCTAssertTrue(store.workouts.isEmpty)
        XCTAssertEqual(store.lastError, tr("activity.loadFailed"))
    }

    @MainActor
    func testCorruptJSONIsPreservedAndResetIsExplicitRecovery() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let original = Data("{broken".utf8)
        try original.write(to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertEqual(try Data(contentsOf: url), original)
        XCTAssertEqual(store.lastError, tr("activity.loadFailed"))
        store.resetAllData()
        XCTAssertNotEqual(try Data(contentsOf: url), original)
        XCTAssertNil(store.lastError)
    }

    @MainActor
    func testManualActivityRoundTripsAndFailedWriteDoesNotPublish() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let day = EpochDay.of(Date())
        let store = ActivityStore(filename: url.lastPathComponent)
        store.addManualEntry(minutes: 12, day: day)
        XCTAssertEqual(store.workouts.count, 1)

        let reloaded = ActivityStore(filename: url.lastPathComponent)
        XCTAssertEqual(reloaded.workouts, store.workouts)

        let blockedName = UUID().uuidString + "/activity.json"
        let blockedURL = fileURL(blockedName)
        defer { try? FileManager.default.removeItem(at: blockedURL.deletingLastPathComponent()) }
        let blocked = ActivityStore(filename: blockedName)
        blocked.addManualEntry(minutes: 10, day: day)
        XCTAssertTrue(blocked.workouts.isEmpty)
        XCTAssertEqual(blocked.lastError, tr("activity.saveFailed"))
    }

    @MainActor
    func testBackupRestoreAndResetReplaceTheWholeActivityStore() throws {
        let sourceURL = fileURL()
        let destinationURL = fileURL()
        defer {
            try? FileManager.default.removeItem(at: sourceURL)
            try? FileManager.default.removeItem(at: destinationURL)
        }
        let source = ActivityStore(filename: sourceURL.lastPathComponent)
        source.addManualEntry(minutes: 18, day: EpochDay.of(Date()))
        let backup = try XCTUnwrap(source.backupData())
        let destination = ActivityStore(filename: destinationURL.lastPathComponent)
        XCTAssertTrue(destination.restoreBackup(backup))
        XCTAssertEqual(destination.workouts, source.workouts)

        let beforeInvalidRestore = try XCTUnwrap(destination.backupData())
        XCTAssertFalse(destination.restoreBackup(Data("{invalid".utf8)))
        XCTAssertEqual(destination.backupData(), beforeInvalidRestore)
        destination.resetAllData()
        let afterReset = ActivityStore(filename: destinationURL.lastPathComponent)
        XCTAssertTrue(afterReset.workouts.isEmpty)
        XCTAssertTrue(afterReset.ownedCosmetics.isEmpty)
    }

    @MainActor
    func testCompletedPracticeSessionPreservesIdentityTimingAndContributesToOverview() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let nowMillis = Int64(Date().timeIntervalSince1970 * 1_000)
        try json([
            "firstUseDay": EpochDay.of(Date()), "workouts": [], "goals": [],
            "practiceDraft": ["token": "session-test", "startedAtMillis": nowMillis - 60_000,
                              "elapsedMillis": 60_000, "isRunning": false],
        ], to: url)
        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertTrue(store.completePractice())
        let session = try XCTUnwrap(store.workouts.first)
        XCTAssertEqual(session.sourceId, "practice:session-test")
        XCTAssertEqual(session.sourceRaw, "PRACTICE_SESSION")
        XCTAssertEqual(session.durationMillis, 60_000)
        XCTAssertEqual(session.startedAtMillis, nowMillis - 60_000)
        XCTAssertNotNil(session.completedAtMillis)
        XCTAssertNil(store.practiceDraft)
        XCTAssertEqual(store.overview().totals.sessions, 1)
        XCTAssertTrue(String(decoding: store.activityCSV(), as: UTF8.self).contains("\(session.epochDay),60000,PRACTICE_SESSION,"))

        let reloaded = ActivityStore(filename: url.lastPathComponent)
        XCTAssertEqual(reloaded.workouts, [session])
        XCTAssertEqual(reloaded.overview().totals.sessions, 1)
    }

    @MainActor
    func testRunningPracticeDraftRecoversPausedAndKeepsItsCheckpoint() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let nowMillis = Int64(Date().timeIntervalSince1970 * 1_000)
        let original = try json([
            "firstUseDay": EpochDay.of(Date()), "workouts": [], "goals": [],
            "practiceDraft": ["token": "interrupted", "startedAtMillis": nowMillis - 30_000,
                              "elapsedMillis": 12_000, "isRunning": true],
        ], to: url)

        let store = ActivityStore(filename: url.lastPathComponent)

        XCTAssertEqual(store.practiceDraft?.isRunning, false)
        XCTAssertEqual(store.practiceElapsedMillis, 12_000)
        XCTAssertNotEqual(try Data(contentsOf: url), original)
        let reopened = ActivityStore(filename: url.lastPathComponent)
        XCTAssertEqual(reopened.practiceDraft?.elapsedMillis, 12_000)
        XCTAssertEqual(reopened.practiceDraft?.isRunning, false)
    }

    @MainActor
    func testRewardClaimOwnershipAndLoadoutPersistAcrossReload() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let today = EpochDay.of(Date())
        let currentMonday = today - Int64(EpochDay.mondayIndex(today) - 1)
        let closedMonday = currentMonday - 7
        try json([
            "firstUseDay": closedMonday,
            "workouts": [["epochDay": closedMonday, "durationMillis": 60_000,
                          "sourceId": "practice:weekly-goal", "sourceRaw": "PRACTICE_SESSION"]],
            "goals": [["effectiveMonday": closedMonday, "sessions": 1, "minutes": NSNull()]],
            "weeks": [["monday": closedMonday, "zoneId": "UTC", "beta": false]],
        ], to: url)
        let store = ActivityStore(filename: url.lastPathComponent)
        let reward = try XCTUnwrap(store.pendingReward)
        XCTAssertTrue(store.claimReward(weekMonday: reward.weekMonday))
        let cosmetic = try XCTUnwrap(ActivityCosmetics.shared.all.first { $0.id == reward.cosmeticId })
        store.equip(cosmetic)

        let reloaded = ActivityStore(filename: url.lastPathComponent)
        XCTAssertTrue(reloaded.ownedCosmetics.contains { $0.cosmeticId == cosmetic.id })
        XCTAssertEqual(reloaded.loadout.first?.cosmeticId, cosmetic.id)
        XCTAssertNotNil(reloaded.claimedReward)
        XCTAssertFalse(reloaded.claimReward(weekMonday: reward.weekMonday))
    }

    @MainActor
    func testFailedRewardClaimWriteChangesNeitherGrantNorOwnership() throws {
        let url = fileURL()
        defer { try? FileManager.default.removeItem(at: url) }
        let today = EpochDay.of(Date())
        let currentMonday = today - Int64(EpochDay.mondayIndex(today) - 1)
        let closedMonday = currentMonday - 7
        let cosmetic = try XCTUnwrap(ActivityCosmetics.shared.all.first)
        try json([
            "firstUseDay": closedMonday,
            "workouts": [], "goals": [],
            "weeks": [["monday": closedMonday, "zoneId": "UTC", "beta": false]],
            "rewards": [["weekMonday": closedMonday, "cosmeticId": cosmetic.id,
                         "result": ActivityRewardsKt.REWARD_RESULT_COSMETIC,
                         "grantedAt": Int64(Date().timeIntervalSince1970 * 1_000), "acknowledgedAt": 0]],
        ], to: url)
        let store = ActivityStore(filename: url.lastPathComponent)
        let reward = try XCTUnwrap(store.pendingReward)

        try FileManager.default.removeItem(at: url)
        try FileManager.default.createDirectory(at: url, withIntermediateDirectories: false)

        XCTAssertFalse(store.claimReward(weekMonday: reward.weekMonday))
        XCTAssertEqual(store.rewardGrants.first?.acknowledgedAt, 0)
        XCTAssertTrue(store.ownedCosmetics.isEmpty)
        XCTAssertEqual(store.lastError, tr("activity.saveFailed"))
    }
}
