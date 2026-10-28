package com.opus.piano

import com.opus.piano.audio.DurableWav
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class DurableWavTest {
    private fun fixture(test:(File)->Unit) { val root=Files.createTempDirectory("gould-wav-test").toFile();try{test(root)}finally{root.deleteRecursively()} }
    @Test fun continuousSamplesIncludeSilenceAndSignedValuesWithoutCreditClock()=fixture { root ->
        val samples=shortArrayOf(Short.MIN_VALUE,-1,0,0,0,1,Short.MAX_VALUE)
        val saved=DurableWav.create(File(root,"pending"),44100,reserveBytes=0).use { wav -> wav.append(samples,samples.size);wav.finish(File(root,"take.wav")) }
        val bytes=saved.file.readBytes();assertEquals(7L,saved.samples);assertEquals(58,bytes.size)
        val pcm=ByteBuffer.wrap(bytes,44,14).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { assertEquals(it,pcm.short) }
        assertEquals(DurableWav.hash(saved.file),saved.sha256)
    }
    @Test fun explicitRecoveryRepairsHeaderAndPreservesCapturedSamples()=fixture { root ->
        val source=File(root,"pending");DurableWav.create(source,16000,reserveBytes=0).use { it.append(ShortArray(16000){i->if(i<8000)123 else 0},16000) }
        val before=source.readBytes().drop(44)
        val saved=DurableWav.recover(source,16000).use { it.finish(File(root,"recovered.wav")) }
        assertEquals(before,saved.file.readBytes().drop(44));assertEquals(16000L,saved.samples)
        assertArrayEquals(DurableWav.header(16000,32000),saved.file.readBytes().take(44).toByteArray())
    }
    @Test fun lowStorageAndSizeLimitStopBeforeOverwritingExistingAudio()=fixture { root ->
        val source=File(root,"pending");var available=1000L
        DurableWav.create(source,8000,maxDataBytes=8,availableBytes={available},reserveBytes=10).use { wav ->
            wav.append(shortArrayOf(1,2),2);val before=source.readBytes();available=10
            assertThrows(IllegalStateException::class.java){wav.append(shortArrayOf(3),1)};assertArrayEquals(before,source.readBytes())
            available=1000;assertThrows(IllegalStateException::class.java){wav.append(ShortArray(3),3)};assertArrayEquals(before,source.readBytes())
        }
    }
    @Test fun existingDestinationAndPendingFilesAreNeverReplaced()=fixture { root ->
        val source=File(root,"pending");val target=File(root,"take.wav");target.writeText("owner data")
        DurableWav.create(source,16000,reserveBytes=0).use { wav ->
            wav.append(shortArrayOf(1),1)
            assertThrows(IllegalStateException::class.java){wav.finish(target)}
            assertThrows(IllegalStateException::class.java){DurableWav.create(source,16000,reserveBytes=0)}
            assertEquals("owner data",target.readText());assertEquals(46L,source.length())
        }
    }
    @Test fun truncatedOddSampleRemainsExportableWithoutSilentRepair()=fixture { root ->
        val source=File(root,"pending");source.writeBytes(DurableWav.header(16000,0)+byteArrayOf(1))
        val before=source.readBytes()
        DurableWav.recover(source,16000).use { wav -> assertThrows(IllegalArgumentException::class.java){wav.finish(File(root,"bad.wav"))} }
        assertArrayEquals(before,source.readBytes());assertFalse(File(root,"bad.wav").exists())
    }
}
