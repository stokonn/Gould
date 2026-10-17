package com.opus.piano.practice

/** Durable boundary; injectable so failure paths run on the host JVM. */
interface PracticePersistence {
    suspend fun recover(): PracticeState?
    suspend fun checkpoint(state: PracticeState)
    suspend fun clear()
    suspend fun complete(state: PracticeState): Long
    suspend fun archiveRecovery(expected: String)
    suspend fun recoveryData(): String?
}
