package com.opus.piano.data.backup

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.nio.charset.CodingErrorAction
import java.security.DigestOutputStream
import java.security.MessageDigest

/** Bounds apply before authoritative writes. Hashes detect damage, not malicious authorship. */
object BackupJson {
    const val MAX_BYTES=32*1024*1024
    private const val MAX_NODES=500000
    fun keys(o:JSONObject,allowed:Set<String>) {require(o.keys().asSequence().toSet()==allowed){"Unexpected or missing backup fields"}}
    fun parse(raw:String):JSONObject {
        require(raw.length<=MAX_BYTES && raw.toByteArray(Charsets.UTF_8).size<=MAX_BYTES){"Backup exceeds 32 MiB; nothing was changed"}
        return parse(StringReader(raw))
    }
    fun parse(file:File):JSONObject {
        val decoder=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
        return android.util.AtomicFile(file).openRead().use {require(file.length()<=MAX_BYTES){"Backup exceeds 32 MiB; nothing was changed"};parse(InputStreamReader(it,decoder))}
    }
    private fun parse(reader:Reader):JSONObject=JsonReader(reader).use { input ->
        input.isLenient=false;var nodes=0
        fun string(s:String):String {require(s.length<=24*1024*1024);var i=0;while(i<s.length){val c=s[i++];if(c.isHighSurrogate()){require(i<s.length && s[i++].isLowSurrogate())}else require(!c.isLowSurrogate())};return s}
        fun value(depth:Int):Any {
            require(depth<=24 && ++nodes<=MAX_NODES){"Backup exceeds safe validation complexity; nothing was changed"}
            return when(input.peek()) {
                JsonToken.BEGIN_OBJECT->{input.beginObject();val o=JSONObject();while(input.hasNext()){val key=string(input.nextName());require(!o.has(key)){"Duplicate backup field"};o.put(key,value(depth+1))};input.endObject();o}
                JsonToken.BEGIN_ARRAY->{input.beginArray();val a=JSONArray();while(input.hasNext())a.put(value(depth+1));input.endArray();a}
                JsonToken.STRING->string(input.nextString())
                JsonToken.NUMBER->{val number=input.nextString();require(number.matches(Regex("-?(0|[1-9][0-9]*)")));number.toLong()}
                JsonToken.BOOLEAN->input.nextBoolean()
                JsonToken.NULL->{input.nextNull();JSONObject.NULL}
                else->error("Invalid backup JSON")
            }
        }
        val root=value(0) as? JSONObject ?: error("Backup object required")
        require(input.peek()==JsonToken.END_DOCUMENT);root
    }
    fun write(value:Any?,out:OutputStream) {
        val writer=JsonWriter(BufferedWriter(OutputStreamWriter(out,Charsets.UTF_8),65536))
        fun emit(v:Any?) {when(v){
            null,JSONObject.NULL->writer.nullValue()
            is JSONObject->{writer.beginObject();v.keys().asSequence().toList().sorted().forEach {k->writer.name(k);emit(v.get(k))};writer.endObject()}
            is JSONArray->{writer.beginArray();for(i in 0 until v.length())emit(v.get(i));writer.endArray()}
            is String->writer.value(v)
            is Boolean->writer.value(v)
            is Int->writer.value(v.toLong())
            is Long->writer.value(v)
            else->error("Unsupported backup value")
        }}
        emit(value);writer.flush()
    }
    fun hash(value:Any):String {
        val digest=MessageDigest.getInstance("SHA-256")
        write(value,DigestOutputStream(object:OutputStream(){override fun write(b:Int){};override fun write(b:ByteArray,off:Int,len:Int){}},digest))
        return digest.digest().joinToString(""){"%02x".format(it)}
    }
    fun encodedSize(value:Any):Long {var size=0L;write(value,object:OutputStream(){override fun write(b:Int){size++};override fun write(b:ByteArray,off:Int,len:Int){size+=len;require(size<=MAX_BYTES){"Backup exceeds 32 MiB; nothing was truncated"}}});return size}
}

/** Unlike JSONObject.getLong, never coerce strings, booleans or floating values. */
internal fun JSONObject.integer(key:String):Long {val v=get(key);require(v is Long || v is Int);return (v as Number).toLong()}
