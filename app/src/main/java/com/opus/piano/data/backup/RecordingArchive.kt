package com.opus.piano.data.backup

import com.opus.piano.audio.DurableWav
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Bounded, streaming personal archive codec. Extraction never touches live files or a database. */
object RecordingArchive {
    const val MAX_ARCHIVE_BYTES = 8L*1024*1024*1024
    const val MAX_ENTRIES = 10_000
    data class Entry(val name:String,val file:File,val sha256:String,val bytes:Long)
    data class Staged(val directory:File,val journal:File,val entries:List<Entry>)
    private fun checkedCopy(source:File,output:OutputStream,bytes:Long,sha:String) {
        val digest=MessageDigest.getInstance("SHA-256");var count=0L
        source.inputStream().use {input->val buffer=ByteArray(64*1024);while(true){val n=input.read(buffer);if(n<0)break;count+=n;require(count<=bytes){"Export source grew; retry"};digest.update(buffer,0,n);output.write(buffer,0,n)}}
        require(count==bytes && digest.digest().joinToString(""){"%02x".format(it)}==sha){"Export source changed; retry"}
    }
    fun validAudioName(name:String):Boolean {
        if(!name.startsWith("audio/"))return false
        val leaf=name.removePrefix("audio/")
        val id=when {leaf.endsWith(".wav.part")->leaf.removeSuffix(".wav.part");leaf.endsWith(".wav")->leaf.removeSuffix(".wav");else->return false}
        return runCatching {UUID.fromString(id).toString()==id}.getOrDefault(false)
    }
    fun write(journal:File,entries:List<Entry>,output:OutputStream) {
        require(journal.length()<=32L*1024*1024 && entries.size<=MAX_ENTRIES)
        require(entries.map {it.name}.distinct().size==entries.size)
        var total=journal.length()
        val manifest=JSONObject().put("format","gould-audio-backup").put("version",1)
            .put("audioFilesIncluded",true).put("journalSha256",DurableWav.hash(journal))
            .put("entries",JSONArray().apply {entries.forEach {e->
                require(validAudioName(e.name) && e.file.isFile && e.file.length()==e.bytes && e.bytes in 0..DurableWav.MAX_DATA_BYTES+44)
                total=Math.addExact(total,e.bytes);require(total<=MAX_ARCHIVE_BYTES)
                require(e.sha256.matches(Regex("[a-f0-9]{64}")))
                put(JSONObject().put("name",e.name).put("sha256",e.sha256).put("bytes",e.bytes))
            }})
        val manifestBytes=manifest.toString().toByteArray(Charsets.UTF_8);require(manifestBytes.size<=4*1024*1024)
        ZipOutputStream(output).use {zip->
            zip.setLevel(1)
            zip.putNextEntry(ZipEntry("manifest.json"));zip.write(manifestBytes);zip.closeEntry()
            zip.putNextEntry(ZipEntry("journal.json"));checkedCopy(journal,zip,journal.length(),manifest.getString("journalSha256"));zip.closeEntry()
            require(DurableWav.hash(journal)==manifest.getString("journalSha256")) {"Journal changed during export"}
            entries.forEach {e->
                zip.putNextEntry(ZipEntry(e.name));checkedCopy(e.file,zip,e.bytes,e.sha256);zip.closeEntry()
                require(e.file.length()==e.bytes && DurableWav.hash(e.file)==e.sha256) {"Recording changed during export; retry after stopping capture"}
            }
        }
    }
    fun stage(input:InputStream,directory:File):Staged {
        check(!directory.exists() && directory.mkdirs()) {"Existing restore staging is retained"}
        val names=linkedSetOf<String>();var total=0L
        ZipInputStream(input).use {zip->
            while(true) {
                val entry=zip.nextEntry ?: break
                require(!entry.isDirectory && (entry.name in setOf("manifest.json","journal.json") || validAudioName(entry.name))) {"Unsafe archive entry"}
                require(names.add(entry.name) && names.size<=MAX_ENTRIES+2) {"Duplicate or excessive archive entries"}
                val file=File(directory,entry.name);require(file.canonicalFile.toPath().startsWith(directory.canonicalFile.toPath()))
                file.parentFile!!.mkdirs();check(file.createNewFile())
                val limit=when(entry.name){"manifest.json"->4L*1024*1024;"journal.json"->32L*1024*1024;else->DurableWav.MAX_DATA_BYTES+44}
                var count=0L
                file.outputStream().use {out->val buffer=ByteArray(64*1024);while(true){val n=zip.read(buffer);if(n<0)break;count+=n;total+=n;require(count<=limit && total<=MAX_ARCHIVE_BYTES){"Archive resource limit exceeded"};out.write(buffer,0,n)}}
                zip.closeEntry()
            }
        }
        return inspect(directory)
    }
    fun inspect(directory:File):Staged {
        val names=directory.walkTopDown().filter {it.isFile}.map {it.relativeTo(directory).invariantSeparatorsPath}
            .filterNot {it in setOf("receipt.json","receipt.json.bak","receipt.json.new")}.toSet()
        require("manifest.json" in names && "journal.json" in names && names.size<=MAX_ENTRIES+2)
        val manifest=BackupJson.parse(File(directory,"manifest.json"))
        BackupJson.keys(manifest,setOf("format","version","audioFilesIncluded","journalSha256","entries"))
        require(manifest.getString("format")=="gould-audio-backup" && manifest.integer("version")==1L && manifest.get("audioFilesIncluded")==true)
        val journal=File(directory,"journal.json");require(DurableWav.hash(journal)==manifest.getString("journalSha256")){"Journal integrity mismatch"}
        val array=manifest.getJSONArray("entries");require(array.length()<=MAX_ENTRIES)
        val declared=mutableSetOf<String>()
        val entries=(0 until array.length()).map {i->
            val row=array.getJSONObject(i);BackupJson.keys(row,setOf("name","sha256","bytes"))
            val name=row.getString("name");require(validAudioName(name) && declared.add(name) && name in names)
            val file=File(directory,name);val bytes=row.integer("bytes");val sha=row.getString("sha256")
            require(bytes==file.length() && sha.matches(Regex("[a-f0-9]{64}")) && DurableWav.hash(file)==sha){"Recording integrity mismatch"}
            Entry(name,file,sha,bytes)
        }
        require(names==declared+setOf("manifest.json","journal.json")){"Undeclared archive content"}
        return Staged(directory,journal,entries)
    }
}
