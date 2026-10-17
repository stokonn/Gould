package com.opus.piano.data

import android.content.Context
import com.opus.piano.R
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** Ten short verified excerpts from Schumann/Pierson (1860); Czech is a labelled Goldberg translation. */
data class MusicQuote(val id: Int, val english: String, val textResource: Int)
object QuoteLibrary {
    const val SOURCE="https://www.gutenberg.org/cache/epub/28219/pg28219.txt"
    val entries=listOf(
        MusicQuote(0,"Play strictly in time!",R.string.quote_0),
        MusicQuote(1,"Learn betimes the fundamental principles of Harmony.",R.string.quote_1),
        MusicQuote(2,"You may play too slow or too fast; both are faults.",R.string.quote_2),
        MusicQuote(3,"Endeavour to play easy pieces well and with elegance; that is better than to play difficult pieces badly.",R.string.quote_3),
        MusicQuote(4,"Frequently sing in choruses, especially the middle parts, this will help to make you a real musician.",R.string.quote_4),
        MusicQuote(5,"Do not neglect to attend good Operas.",R.string.quote_5),
        MusicQuote(6,"Highly esteem the Old, but take also a warm interest in the New.",R.string.quote_6),
        MusicQuote(7,"Look deeply into life, and study it as diligently as the other arts and sciences.",R.string.quote_7),
        MusicQuote(8,"By means of industry and perseverance you will rise higher and higher.",R.string.quote_8),
        MusicQuote(9,"Without enthusiasm nothing great can be effected in art.",R.string.quote_9)
    )
    fun forDay(day: LocalDate): MusicQuote=entries[Math.floorMod(day.toEpochDay(),10).toInt()]
    fun verified(text: String, quote: MusicQuote): Boolean = text.replace("_", "").replace(Regex("\\s+")," ").contains(quote.english)
}
class DailyQuotes(context: Context, private val fetch: suspend () -> String = { fetchSource() }) {
    private val prefs=context.getSharedPreferences("goldberg-quotes",Context.MODE_PRIVATE)
    private val gate=Mutex()
    suspend fun daily(day: LocalDate): MusicQuote=withContext(Dispatchers.IO) { gate.withLock {
        val quote=QuoteLibrary.forDay(day)
        // Persist attempt before IO; no repeated fetches on recomposition, reopen or process restart.
        if(prefs.getString("attempt",null)!=day.toString()) {
            if(prefs.edit().putString("attempt",day.toString()).commit()) try {
                val source=withTimeout(8000) { fetch() }
                if(QuoteLibrary.verified(source,quote)) prefs.edit().putString("day",day.toString()).putInt("id",quote.id).putString("text",quote.english).commit()
            } catch(c:CancellationException) { if(c !is TimeoutCancellationException) throw c }
            catch(_:Exception) { /* The verified bundled entry is always available. */ }
        }
        val cached=QuoteLibrary.entries.firstOrNull {it.id==prefs.getInt("id",-1) && it.english==prefs.getString("text",null)}
        if(prefs.getString("day",null)==day.toString() && cached?.id==quote.id) cached else quote
    } }
    companion object {
        private suspend fun fetchSource(): String=withContext(Dispatchers.IO) {
            val connection=URL(QuoteLibrary.SOURCE).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout=4000;connection.readTimeout=4000;connection.instanceFollowRedirects=false
                connection.setRequestProperty("User-Agent","Goldberg/0.9.2 (public-domain music quotation)")
                check(connection.responseCode==200)
                connection.inputStream.use { input ->
                    val bytes=input.readBytesLimited(128*1024)
                    bytes.toString(Charsets.UTF_8)
                }
            } finally {connection.disconnect()}
        }
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val output=java.io.ByteArrayOutputStream();val buffer=ByteArray(4096)
    while(true) { val count=read(buffer);if(count<0) break;require(output.size()+count<=limit);output.write(buffer,0,count) }
    return output.toByteArray()
}
