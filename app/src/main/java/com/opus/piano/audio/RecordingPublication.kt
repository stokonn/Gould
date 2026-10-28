package com.opus.piano.audio

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption

/** All publishers of private recording names share this directory lock, including restore.
 * Android app-data SELinux disallows hard links. Atomic rename is supported, but can
 * replace an existing target: serialize every app-owned publisher, then check inside
 * the cross-process lock. A collision always leaves the original and source intact.
 * Do not delete the lock file: its inode is the shared synchronization identity. */
object RecordingPublication {
    private val localGate = Any()

    fun publish(source: File, destination: File, keepSource: Boolean = false) = synchronized(localGate) {
        val directory = requireNotNull(destination.parentFile).canonicalFile
        require(source.isFile && source.canonicalFile != destination.canonicalFile)
        check(directory.isDirectory || directory.mkdirs())
        RandomAccessFile(File(directory, ".publication.lock"), "rw").use { guard ->
            guard.channel.lock().use {
                check(!Files.exists(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) { "Recording destination already exists" }
                if (!keepSource) {
                    require(source.parentFile?.canonicalFile == directory)
                    Files.move(source.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
                } else {
                    // Restore retains its verified archive/staging even after successful publication.
                    val staged = File.createTempFile(".publication-", ".pending", directory)
                    try {
                        FileOutputStream(staged).use { output ->
                            source.inputStream().use { it.copyTo(output, 64 * 1024) }; output.fd.sync()
                        }
                        check(staged.length() == source.length() && DurableWav.hash(staged) == DurableWav.hash(source))
                        Files.move(staged.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
                    } finally {
                        // This uniquely created duplicate is disposable; the archive original remains intact.
                        staged.delete()
                    }
                }
            }
        }
    }
}
