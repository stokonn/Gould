package com.opus.piano.data.community.canonical

import com.opus.piano.admin.AdminHttpResponse
import com.opus.piano.admin.AdminTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

/** Only compiled by -PgoldbergCanonicalIntegration=true with a disposable PG
 * fixture. This executes real Kotlin HTTP, the actual BFF and PostgreSQL. */
class CanonicalBffHttpIntegrationTest {
    @Test fun canonicalApplicationServiceAuthenticatesReadsReviewsAndLogsOutOverActualBff() = runBlocking {
        val source = File(requireNotNull(System.getenv("GOLDBERG_BFF_SOURCE"))).canonicalFile
        val server = File(source, "services/community-bff/tests/client-server.mjs")
        require(server.isFile && System.getenv("SCHEMA_PG_PORT") != null)
        val process = ProcessBuilder("node", server.path).directory(source).redirectError(ProcessBuilder.Redirect.INHERIT).start()
        try {
            val startup = JSONObject(withContext(Dispatchers.IO) {
                val reader = process.inputStream.bufferedReader()
                generateSequence { reader.readLine() }.take(10).first { it.startsWith("{\"url\":") }
            })
            val base = startup.getString("url")
            require(URI(base).host == "127.0.0.1" && URI(base).scheme == "http")
            val transport = AdminTransport { method, url, headers, body -> withContext(Dispatchers.IO) {
                val path = URI(url).rawPath + (URI(url).rawQuery?.let { "?$it" } ?: "")
                val connection = URL(base + path).openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = method; connection.instanceFollowRedirects = false
                    connection.connectTimeout = 5000; connection.readTimeout = 10000
                    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
                    body?.let { connection.doOutput = true; connection.outputStream.use { out -> out.write(it) } }
                    val status = connection.responseCode
                    val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                    val bytes = stream?.use { it.readBytes() } ?: byteArrayOf()
                    require(bytes.size <= 524288)
                    AdminHttpResponse(status, bytes, connection.getHeaderField("Retry-After")?.toLongOrNull())
                } finally { connection.disconnect() }
            } }
            val root = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
                .first { File(it, "contracts/admin/v1/decision-proposal/dto.schema.json").exists() }
            val codec = AdminContract(File(root, "contracts/admin/v1/decision-proposal/dto.schema.json").readText())
            val client = CanonicalAdminClient(codec, CanonicalHttpTransport({ "https://fixture.invalid" }, transport))
            val repo = CanonicalAdminRepository(client)
            val challenge = repo.challenge().json()
            val now = System.currentTimeMillis()
            val credential = JSONObject().put("issuer", "https://synthetic.invalid").put("audience", "local-test")
                .put("subject", "admin").put("nonce", challenge.getString("nonce")).put("iat", now).put("exp", now + 120000).toString()
            repo.authenticate(challenge.getString("challenge"), credential)
            assertTrue(repo.authenticated)
            assertEquals(1, repo.queue().json().getJSONArray("items").length())
            val id = startup.getString("submissionId")
            assertEquals(id, repo.detail(id).json().getString("submissionId"))
            val review = repo.review(id)
            assertEquals("0", review.detail.json().getString("expectedEntityRevision"))
            assertEquals("in_review", repo.open(review).json().getString("status"))
            val fresh = repo.review(id)
            assertEquals("rejected", repo.confirm(fresh, "reject", "Synthetic integration rejection", true).json().getString("status"))
            assertNotNull(repo.statistics()); assertNotNull(repo.audit())
            assertTrue(repo.logout().revocationConfirmed)
            assertFalse(repo.authenticated); assertEquals(CanonicalAdminState(), repo.state.value)
        } finally {
            process.outputStream.use { it.write(10) }
            if (!process.waitFor(15, TimeUnit.SECONDS)) { process.destroy(); if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly() }
        }
    }
}
