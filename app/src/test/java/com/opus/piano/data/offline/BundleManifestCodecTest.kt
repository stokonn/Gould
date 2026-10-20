package com.opus.piano.data.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stage 5 regression tests for the durable manifest codec.
 *
 * Focus: round-trip fidelity, version/size bounds, path traversal, hash/MIME shape and
 * mandatory rights. Executed on the JVM host; no emulator required.
 */
class BundleManifestCodecTest {

    private val rights = BundleRights(
        license = "CC0 1.0",
        licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/",
        attribution = "Open Opus contributors",
        sourceName = "Open Opus",
        sourceUrl = "https://example.org/source"
    )

    private fun file(
        sha: String = "a".repeat(64),
        path: String = "media/portrait.jpg",
        mime: String = "image/jpeg",
        rights: BundleRights = this.rights,
        language: String? = "en"
    ) = BundleFile(
        sha256 = sha, bytes = 1234L, mime = mime, relativePath = path,
        language = language, rights = rights
    )

    private fun manifest(
        state: BundleState = BundleState.READY,
        validatedAt: Long? = 2000L,
        attribution: String = "Open Opus contributors",
        files: List<BundleFile> = listOf(file())
    ) = BundleManifest(
        bundleId = "12345678-1234-1234-1234-123456789abc",
        type = BundleType.WORK,
        entityId = "work-42",
        schemaVersion = 1,
        revision = 3,
        language = "en",
        createdAt = 1000L,
        validatedAt = validatedAt,
        dependencies = listOf(),
        expectedBytes = 1234L,
        files = files,
        providerKind = "open-opus",
        providerRevision = "2026-10",
        attribution = attribution,
        minReaderVersion = 1,
        state = state,
        progress = BundleProgress(1234L, 1234L, 1),
        lastError = null,
        updatedAt = 3000L
    )

    // ------------------------------------------------------------ round trip

    @Test
    fun roundTripPreservesReadyManifestExactly() {
        val original = manifest()
        val decoded = BundleManifestCodec.decode(BundleManifestCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun roundTripPreservesPartialProgressAndNullFields() {
        val original = manifest(
            state = BundleState.PAUSED,
            validatedAt = null,
            files = listOf(file(language = null))
        ).copy(progress = BundleProgress(100L, 4000L, 1), lastError = "offline")
        val decoded = BundleManifestCodec.decode(BundleManifestCodec.encode(original))
        assertEquals(original, decoded)
    }

    // ------------------------------------------------------------ version & size

    @Test
    fun decodeRejectsUnknownFormatVersion() {
        val raw = BundleManifestCodec.encode(manifest())
            .replace("\"formatVersion\":1", "\"formatVersion\":999")
        val error = assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.decode(raw)
        }
        assertTrue(error.message!!.contains("formatVersion"))
    }

    @Test
    fun decodeRejectsOversizedPayload() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.decode("x".repeat(BundleManifestCodec.MAX_ENCODED_BYTES + 1))
        }
    }

    @Test
    fun encodeRejectsOversizedManifest() {
        val huge = manifest(attribution = "x".repeat(BundleManifestCodec.MAX_ENCODED_BYTES + 16))
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.encode(huge)
        }
    }

    @Test
    fun decodeRejectsMissingRequiredField() {
        val raw = BundleManifestCodec.encode(manifest())
            .replace("\"bundleId\":\"12345678-1234-1234-1234-123456789abc\",", "")
        assertThrows(IllegalArgumentException::class.java) { BundleManifestCodec.decode(raw) }
    }

    @Test
    fun decodeRejectsUnknownEnumValues() {
        val raw = BundleManifestCodec.encode(manifest())
            .replace("\"state\":\"READY\"", "\"state\":\"NOT_A_STATE\"")
        val error = assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.decode(raw)
        }
        assertTrue(error.message!!.contains("state"))
    }

    @Test
    fun decodeRejectsStateThatContradictsValidationTimestamp() {
        // DOWNLOADING with validatedAt would violate the model invariant.
        val raw = BundleManifestCodec.encode(manifest())
            .replace("\"state\":\"READY\"", "\"state\":\"DOWNLOADING\"")
        assertThrows(IllegalArgumentException::class.java) { BundleManifestCodec.decode(raw) }
    }

    // ------------------------------------------------------------ path safety

    @Test
    fun requirePathAcceptsOrdinaryRelativePaths() {
        listOf(
            "media/portrait.jpg",
            "text/work-note.md",
            "a/b/c/deep.bin",
            "file.txt"
        ).forEach { BundleManifestCodec.requirePath(it) }
    }

    @Test
    fun requirePathRejectsTraversalAbsoluteAndControlPaths() {
        val nulPath = "media" + String(CharArray(1)) + "portrait.jpg"
        listOf(
            "../evil.jpg",
            "a/../../b",
            "..",
            ".",
            "/absolute/path.jpg",
            "a//b",
            "trailing/",
            "C:evil.txt",
            "x:ads",
            "win\\path.jpg",
            ".git/config",
            "a/.git/hooks/pre-commit",
            nulPath,
            "",
            "a".repeat(BundleManifestCodec.MAX_PATH_CHARS + 1)
        ).forEach { path ->
            assertThrows("expected rejection for: $path", IllegalArgumentException::class.java) {
                BundleManifestCodec.requirePath(path)
            }
        }
    }

    // ------------------------------------------------------------ file validation

    @Test
    fun validateFileRejectsMalformedHashes() {
        listOf(
            "A".repeat(64),                 // uppercase
            "a".repeat(63),                 // too short
            "a".repeat(65),                 // too long
            "z".repeat(64),                 // non-hex
            ""
        ).forEach { sha ->
            assertThrows("expected rejection for sha: $sha", IllegalArgumentException::class.java) {
                BundleManifestCodec.validateFile(file(sha = sha))
            }
        }
    }

    @Test
    fun validateFileRejectsMalformedMime() {
        listOf("not-a-mime", "text/plain; charset=utf-8", "", "image", "/jpeg").forEach { mime ->
            assertThrows("expected rejection for mime: $mime", IllegalArgumentException::class.java) {
                BundleManifestCodec.validateFile(file(mime = mime))
            }
        }
    }

    @Test
    fun validateFileAcceptsTypicalMimeTypes() {
        listOf("image/jpeg", "application/json", "text/plain", "application/ld+json", "audio/mpeg")
            .forEach { mime -> BundleManifestCodec.validateFile(file(mime = mime)) }
    }

    @Test
    fun validateFileRejectsMissingMandatoryRights() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(rights = rights.copy(license = " ")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(rights = rights.copy(attribution = "")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(rights = rights.copy(sourceName = "")))
        }
    }

    @Test
    fun validateFileRejectsInsecureUrls() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(rights = rights.copy(licenseUrl = "http://example.org/l")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(rights = rights.copy(sourceUrl = "ftp://example.org/s")))
        }
        // Absent URLs stay legal; mandatory fields are name/license/attribution/source names.
        BundleManifestCodec.validateFile(
            file(rights = rights.copy(licenseUrl = null, sourceUrl = null))
        )
    }

    @Test
    fun validateFileRejectsBlankLanguage() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.validateFile(file(language = " "))
        }
    }

    @Test
    fun encodeValidatesPathsBeforeWriting() {
        val bad = manifest(files = listOf(file(path = "../escape.jpg")))
        assertThrows(IllegalArgumentException::class.java) {
            BundleManifestCodec.encode(bad)
        }
    }

    @Test
    fun decodeValidatesTamperedPathEvenWhenSignatureFieldsMatch() {
        val raw = BundleManifestCodec.encode(manifest())
            .replace("\"relativePath\":\"media/portrait.jpg\"", "\"relativePath\":\"../portrait.jpg\"")
        assertThrows(IllegalArgumentException::class.java) { BundleManifestCodec.decode(raw) }
    }

    // ------------------------------------------------------------ model guards

    @Test
    fun manifestRejectsDuplicateContentReferences() {
        assertThrows(IllegalArgumentException::class.java) {
            manifest(files = listOf(file(), file(path = "media/other.jpg")))
        }
    }

    @Test
    fun manifestRejectsBlankAttribution() {
        assertThrows(IllegalArgumentException::class.java) {
            manifest(attribution = " ")
        }
    }

    @Test
    fun progressRejectsInconsistentCounts() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleProgress(completedBytes = 10L, totalBytes = 5L, completedFiles = 0)
        }
    }
}
