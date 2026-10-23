package com.opus.piano

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.community.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Run only against an explicitly targeted emulator, with synthetic credentials. */
@RunWith(AndroidJUnit4::class)
class CommunityLocalInstrumentedTest {
    private fun context(): Context {
        val app=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(app.noBackupFilesDir,"community-instrumentation-"+UUID.randomUUID()).apply {mkdirs()}
        return object: ContextWrapper(app) {override fun getNoBackupFilesDir()=root}
    }
    @Test fun keystoreRoundTripEncryptsTokenAndSignOutClearsIt() {
        val c=context();val vault=AndroidCommunityCredentials(c);val owner=UUID.randomUUID().toString()
        val fixture="synthetic-emulator-token-no-real-credentials"
        vault.store(CommunitySession(owner,fixture,Long.MAX_VALUE,true))
        assertFalse(File(c.noBackupFilesDir,"community-credentials.v1").readBytes().toString(Charsets.ISO_8859_1).contains(fixture))
        val restored=AndroidCommunityCredentials(c).current()!!;assertEquals(owner,restored.principalId);assertEquals(fixture,restored.credential)
        vault.clear();assertNull(AndroidCommunityCredentials(c).current());assertFalse(File(c.noBackupFilesDir,"community-credentials.v1").exists())
    }
    @Test fun credentialsCannotBeReadAfterCiphertextTampering() {
        val c=context();AndroidCommunityCredentials(c).store(CommunitySession(UUID.randomUUID().toString(),"synthetic-invalidated-token",Long.MAX_VALUE,true))
        val file=File(c.noBackupFilesDir,"community-credentials.v1");val data=file.readBytes();data[data.lastIndex]=(data.last().toInt() xor 1).toByte();file.writeBytes(data)
        assertNull(AndroidCommunityCredentials(c).current())
    }
    @Test fun installedApplicationDisablesAndroidBackup() {
        val c=InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(0,c.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
        assertFalse(c.noBackupFilesDir.canonicalPath==c.filesDir.canonicalPath)
    }
    @Test fun independentStoreInstancesRecoverLocalDraftOnAndroid()=runBlocking {
        val c=context();val root=File(c.noBackupFilesDir,"drafts");val store=CommunityDraftStore(root);val owner=store.guest()
        val p=ContributionPayload(CatalogueUuid(UUID.randomUUID().toString()),CommunityKind.WORK,listOf(ContributionField("title","Synthetic local draft",listOf(SourceEvidence("reference","Authored synthetic fixture","CC0-1.0")))),"CC0-1.0")
        val saved=store.save(CommunityDraft(UUID.randomUUID().toString(),owner,0,p),0)
        assertEquals(saved,CommunityDraftStore(root).get(owner,saved.id))
    }
    @Test fun applicationCommunityRemainsDisabledWithoutChangingPracticeDatabase()=runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as OpusApplication
        assertEquals(CommunityAvailability.DISABLED,app.community.availability)
        val p=ContributionPayload(CatalogueUuid(UUID.randomUUID().toString()),CommunityKind.WORK,emptyList(),"")
        // Use an isolated test root rather than modifying the app's actual drafts.
        val c=context();val credentials=AndroidCommunityCredentials(c)
        val repo=LocalCommunityRepository(CommunityDraftStore(File(c.noBackupFilesDir,"drafts")),credentials,cache=CommunityOverlayCache(File(c.noBackupFilesDir,"public.json")))
        val d=repo.create(p);assertEquals(CommunityProblem.DISABLED,(repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertEquals(d,repo.drafts().single())
    }
}
