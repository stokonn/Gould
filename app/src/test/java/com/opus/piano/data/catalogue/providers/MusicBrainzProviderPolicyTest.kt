package com.opus.piano.data.catalogue.providers

import kotlinx.coroutines.runBlocking
import com.opus.piano.data.catalogue.isActiveCatalogueSearchProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class MusicBrainzProviderPolicyTest {
    @Test fun activeCatalogueSearchAcceptsMusicBrainzCoreOnly() {
        assertTrue(isActiveCatalogueSearchProvider("musicbrainz"))
        assertFalse(isActiveCatalogueSearchProvider("wikidata"))
        assertFalse(isActiveCatalogueSearchProvider("openopus"))
    }

    @Test fun requestsShareAtLeastOneSecondPerClientGate() = runBlocking {
        val adapter = MusicBrainzAdapter()
        val sentAt = mutableListOf<Long>()
        val client = ProviderClient(ProviderTransport {
            sentAt += System.nanoTime() / 1_000_000
            ProviderResponse(404, byteArrayOf())
        }, { true })
        val requests = adapter.search("Bach", "works", "en", 0) + adapter.search("Bach", "works", "en", 20)
        requests.forEach { client.fetch(adapter, it) }
        assertEquals(2, sentAt.size)
        assertTrue("interval was ${sentAt[1] - sentAt[0]} ms", sentAt[1] - sentAt[0] >= 1000)
    }

    @Test fun httpsTransportSendsTheDescriptiveGouldUserAgent() = runBlocking {
        val connection = object : HttpURLConnection(URL("https://musicbrainz.org/")) {
            override fun disconnect() = Unit
            override fun usingProxy() = false
            override fun connect() = Unit
            override fun getResponseCode() = 503
            override fun getErrorStream() = ByteArrayInputStream(byteArrayOf())
            override fun getContentLengthLong() = 0L
        }
        val request = MusicBrainzAdapter().search("Bach", "works", "en").single()
        HttpsProviderTransport(connect = { connection }, allowed = { true }).get(request)
        assertEquals("Gould/0.15.0 (https://github.com/stokonn/Gould)", connection.getRequestProperty("User-Agent"))
    }
}
