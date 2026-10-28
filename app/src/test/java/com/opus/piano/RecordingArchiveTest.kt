package com.opus.piano

import com.opus.piano.audio.DurableWav
import com.opus.piano.data.backup.RecordingArchive
import org.junit.Assert.*
import org.junit.Test
import java.io.*
import java.nio.file.Files
import java.util.zip.*

class RecordingArchiveTest {
    private fun fixture(test:(File)->Unit){val root=Files.createTempDirectory("gould-archive-test").toFile();try{test(root)}finally{root.deleteRecursively()}}
    @Test fun pathsRejectTraversalAbsoluteNamesUnknownExtensionsAndNoncanonicalIds(){
        for(name in listOf("../audio/a.wav","/audio/a.wav","audio/../../a.wav","audio/a.wav","audio/00000000-0000-0000-0000-000000000000.mp3"))assertFalse(name,RecordingArchive.validAudioName(name))
        assertTrue(RecordingArchive.validAudioName("audio/00000000-0000-0000-0000-000000000000.wav.part"))
    }
    @Test fun exportStreamsKnownBytesAndIncludesExplicitMediaManifest()=fixture {root->
        val journal=File(root,"journal");journal.writeText("{}")
        val audio=File(root,"audio");audio.writeBytes(byteArrayOf(1,2,0,0,3,4))
        val output=ByteArrayOutputStream()
        RecordingArchive.write(journal,listOf(RecordingArchive.Entry("audio/00000000-0000-0000-0000-000000000000.wav.part",audio,DurableWav.hash(audio),audio.length())),output)
        val names=mutableListOf<String>()
        ZipInputStream(output.toByteArray().inputStream()).use {zip->while(true){val e=zip.nextEntry?:break;names+=e.name;val content=zip.readBytes();if(e.name.startsWith("audio/"))assertArrayEquals(audio.readBytes(),content);if(e.name=="manifest.json")assertTrue(content.toString(Charsets.UTF_8).contains("\"audioFilesIncluded\":true"))}}
        assertEquals(3,names.size)
    }
    @Test fun unsafeEntryNeverEscapesIsolatedStaging()=fixture {root->
        val output=ByteArrayOutputStream();ZipOutputStream(output).use {it.putNextEntry(ZipEntry("../owner.txt"));it.write("bad".toByteArray());it.closeEntry()}
        assertThrows(IllegalArgumentException::class.java){RecordingArchive.stage(output.toByteArray().inputStream(),File(root,"staging"))}
        assertFalse(File(root,"owner.txt").exists())
    }
    @Test fun existingStagingIsRejectedWithoutReplacement()=fixture {root->
        val existing=File(root,"staging");existing.mkdirs();File(existing,"owner").writeText("retained")
        assertThrows(IllegalStateException::class.java){RecordingArchive.stage(byteArrayOf().inputStream(),existing)}
        assertEquals("retained",File(existing,"owner").readText())
    }
    @Test fun duplicateZipNamesFailBeforeJournalOrLiveDataIsRead()=fixture {root->
        val output=ByteArrayOutputStream();ZipOutputStream(output).use {zip->
            for(name in listOf("journal.json","journam.json")){zip.putNextEntry(ZipEntry(name));zip.write("{}".toByteArray());zip.closeEntry()}
        }
        // Same-length filename substitution preserves payload CRC; it creates a real duplicate ZIP.
        val bytes=output.toByteArray();val from="journam.json".toByteArray();val to="journal.json".toByteArray()
        for(i in 0..bytes.size-from.size)if(from.indices.all {bytes[i+it]==from[it]})to.copyInto(bytes,i)
        assertThrows(IllegalArgumentException::class.java){RecordingArchive.stage(bytes.inputStream(),File(root,"staging"))}
        assertEquals(listOf("journal.json"),File(root,"staging").list()!!.toList())
    }
}
