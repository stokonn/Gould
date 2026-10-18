package com.opus.piano.data.backup

import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** App-private publication is atomic. SAF provider streams do not promise atomic replacement. */
class BackupFiles(private val available:(File)->Long={it.usableSpace},private val fail:(String)->Unit={}) {
    fun publish(file:File,document:JSONObject):File {
        file.parentFile!!.mkdirs();val size=BackupJson.encodedSize(document)
        require(available(file.parentFile!!)>=size+65536){"Insufficient storage; previous backup and journal retained"}
        val atomic=AtomicFile(file);val stream=atomic.startWrite()
        try {
            fail("export_started")
            BackupJson.write(document,object:OutputStream(){override fun write(b:Int){stream.write(b)};override fun write(b:ByteArray,off:Int,len:Int){stream.write(b,off,len);fail("export_chunk")}})
            fail("export_before_publish");atomic.finishWrite(stream);return file
        } catch(t:Throwable){atomic.failWrite(stream);throw t}
    }
    fun receive(file:File,input:InputStream):File {
        file.parentFile!!.mkdirs();val atomic=AtomicFile(file);val out=atomic.startWrite()
        try {val buffer=ByteArray(8192);var size=0L;while(true){val n=input.read(buffer);if(n<0)break;size+=n;require(size<=BackupJson.MAX_BYTES){"Backup exceeds 32 MiB; nothing was changed"};out.write(buffer,0,n)};atomic.finishWrite(out);return file}
        catch(t:Throwable){atomic.failWrite(out);throw t}
    }
}
