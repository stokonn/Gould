package com.opus.piano

import com.opus.piano.audio.RecordingPublication
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RecordingPublicationTest {
    private fun fixture(test: (File) -> Unit) {
        val directory = Files.createTempDirectory("gould-publish").toFile()
        try { test(directory) } finally { directory.deleteRecursively() }
    }
    @Test fun movePublishesCompleteFileAndRetainsPersistentLockIdentity() = fixture { directory ->
        val source = File(directory, "staging").apply { writeText("verified samples") }
        val target = File(directory, "take.wav")
        RecordingPublication.publish(source, target)
        assertEquals("verified samples", target.readText()); assertFalse(source.exists())
        assertTrue(File(directory, ".publication.lock").isFile)
    }
    @Test fun existingFileCannotBeReplacedAndSourceRemainsRecoverable() = fixture { directory ->
        val source = File(directory, "staging").apply { writeText("new samples") }
        val target = File(directory, "take.wav").apply { writeText("owner original") }
        assertThrows(IllegalStateException::class.java) { RecordingPublication.publish(source, target) }
        assertEquals("owner original", target.readText()); assertEquals("new samples", source.readText())
    }
    @Test fun racingPublishersProduceOneWinnerAndRetainLoser() = fixture { directory ->
        val sources = listOf("one", "two").map { name -> File(directory, name).apply { writeText(name) } }
        val target = File(directory, "take.wav"); val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = sources.map { source -> executor.submit<Boolean> {
                start.await(); runCatching { RecordingPublication.publish(source, target) }.isSuccess
            } }
            start.countDown()
            assertEquals(1, results.count { it.get(5, TimeUnit.SECONDS) })
            assertEquals(1, sources.count { it.exists() })
            assertTrue(target.readText() in listOf("one", "two"))
            assertNotEquals(target.readText(), sources.single { it.exists() }.readText())
        } finally { executor.shutdownNow() }
    }
    @Test fun restoreCopyPreservesVerifiedArchiveAndRefusesCollision() = fixture { directory ->
        val staged = File(directory, "archive").apply { mkdirs() }
        val source = File(staged, "take.wav").apply { writeBytes(ByteArray(150_000) { (it % 251).toByte() }) }
        val target = File(directory, "take.wav")
        RecordingPublication.publish(source, target, keepSource = true)
        assertArrayEquals(source.readBytes(), target.readBytes()); assertTrue(source.isFile)
        assertThrows(IllegalStateException::class.java) { RecordingPublication.publish(source, target, keepSource = true) }
        assertArrayEquals(source.readBytes(), target.readBytes())
    }
}
