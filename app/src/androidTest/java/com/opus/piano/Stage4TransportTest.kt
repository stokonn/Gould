package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class Stage4TransportTest {
    private class Connection(val source:InputStream,val code:Int=200):HttpURLConnection(URL("https://api.openopus.org/fixture")) {
        @Volatile var disconnected=false
        override fun disconnect(){disconnected=true;source.close()}
        override fun usingProxy()=false
        override fun connect(){}
        override fun getResponseCode()=code
        override fun getInputStream()=source
        override fun getErrorStream()=source
        override fun getContentLengthLong()=-1L
    }
    private val request=ProviderRequest("https://api.openopus.org/fixture","openopus","search")
    @Test fun oversizedStreamsAndNetworkPolicyRevocationFailBeforeAcceptance()=runBlocking {
        val bytes=ByteArray(ProviderJson.MAX_BYTES+1)
        val connection=Connection(ByteArrayInputStream(bytes))
        val transport=HttpsProviderTransport(connect={connection},allowed={true})
        assertTrue(runCatching {transport.get(request)}.isFailure);assertTrue(connection.disconnected)
        var permitted=true
        val stream=object:InputStream(){var read=false;override fun read():Int {permitted=false;return if(read)-1 else 1.also {read=true}};override fun read(buffer:ByteArray,off:Int,len:Int):Int {if(read)return -1;read=true;permitted=false;buffer[off]=1;return 1}}
        val revoked=Connection(stream)
        assertTrue(runCatching {HttpsProviderTransport(connect={revoked},allowed={permitted}).get(request)}.isFailure);assertTrue(revoked.disconnected)
    }
    @Test fun cancellationClosesBlockingReadWithoutWaitingForSocketTimeout()=runBlocking {
        val entered=CountDownLatch(1);val closed=CountDownLatch(1)
        val stream=object:InputStream(){override fun read():Int {entered.countDown();closed.await(5,TimeUnit.SECONDS);return -1};override fun read(b:ByteArray,off:Int,len:Int)=read();override fun close(){closed.countDown()}}
        val connection=Connection(stream);val transport=HttpsProviderTransport(connect={connection},allowed={true})
        val job=launch(Dispatchers.IO){transport.get(request)}
        assertTrue(entered.await(5,TimeUnit.SECONDS))
        withTimeout(2000){job.cancelAndJoin()};assertTrue(connection.disconnected);assertEquals(0L,closed.count)
    }
    @Test fun redirectsAndUnexpectedHostsDoNotBecomeContentOrForwardCredentials()=runBlocking {
        val redirected=Connection(ByteArrayInputStream(byteArrayOf()),302)
        val response=HttpsProviderTransport(connect={redirected},allowed={true}).get(request)
        assertEquals(302,response.code);assertEquals(0,response.body.size)
        var opened=false
        val transport=HttpsProviderTransport(connect={opened=true;redirected},allowed={true})
        assertTrue(runCatching {transport.get(request.copy(url="https://attacker.example/fixture"))}.isFailure)
        assertTrue(runCatching {transport.get(request.copy(url="https://user:secret@api.openopus.org/fixture"))}.isFailure);assertFalse(opened)
    }
}
