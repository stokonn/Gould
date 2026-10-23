package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AdminAuditSyncTest {
    @Test fun allOptInsAreOffAndWifiIsDefault() {
        val p=AdminSyncPolicy()
        assertFalse(p.background); assertFalse(p.notifications); assertFalse(p.mobileConsent)
        assertFalse(p.permitsNetwork(true,false)); assertFalse(p.permitsNotification(true))
    }
    @Test fun wifiOnlyRequiresWifiAndUnmeteredNetwork() {
        val p=AdminSyncPolicy(background=true)
        assertTrue(p.permitsNetwork(true,false)); assertFalse(p.permitsNetwork(true,true))
        assertFalse(p.permitsNetwork(false,false)); assertFalse(p.permitsNetwork(false,true))
    }
    @Test fun explicitMobileConsentPermitsMeteredChecks() {
        assertTrue(AdminSyncPolicy(background=true,mobileConsent=true).permitsNetwork(false,true))
    }
    @Test fun notificationsNeedBothOptInAndPlatformPermission() {
        assertFalse(AdminSyncPolicy().permitsNotification(true))
        assertFalse(AdminSyncPolicy(notifications=true).permitsNotification(false))
        assertTrue(AdminSyncPolicy(notifications=true).permitsNotification(true))
    }
    @Test fun incompleteSnapshotNeverProvidesNotificationSignature()=runBlocking {
        var calls=0
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->calls++;AdminHttpResponse(200,"""{"items":[],"nextCursor":"next"}""".toByteArray(),null)})
        assertNull(queueSignature(api,1)); assertEquals(1,calls)
    }
    @Test fun failedSnapshotNeverProvidesNotificationSignature()=runBlocking {
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(503,"{}".toByteArray(),null)})
        assertNull(queueSignature(api))
    }
    @Test fun snapshotDigestDetectsRevisionStatusAndRemovalWithoutStoringPayloads()=runBlocking {
        var rev=1
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(200,"""{"items":[{"id":"s","status":"submitted","revision":$rev}]}""".toByteArray(),null)})
        val first=queueSignature(api); rev=2; val second=queueSignature(api)
        assertNotEquals(first,second); assertEquals(64,first!!.length)
    }
    @Test fun auditDeduplicatesPagesAndFiltersOnlyFetchedRows()=runBlocking {
        var page=0
        val event="""{"seq":1,"ts":100,"actor":"m","action":"decision.approve","subject":"submission:s"}"""
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->
            page++;AdminHttpResponse(200,(if(page==1) """{"items":[$event],"nextCursor":"next"}"""
                else """{"items":[$event,{"seq":2,"ts":100,"actor":"other","action":"session.created","subject":"p"}]}""").toByteArray(),null)
        })
        val s=AdminAuditService(api); s.load(); s.load()
        assertEquals(2,s.state.value.items.size)
        assertEquals(1,s.filtered("s","mine","m").size)
        assertEquals(1,s.filtered("","moderation","m").size)
        assertTrue(s.filtered("missing","all","m").isEmpty())
    }
    @Test fun failedAuditReloadPreservesPreviousEntries()=runBlocking {
        var code=200
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(code,"""{"items":[{"seq":1,"ts":100}]}""".toByteArray(),null)})
        val s=AdminAuditService(api); s.load(); code=503; s.load(reset=true)
        assertEquals(1,s.state.value.items.size); assertNotNull(s.state.value.failure); assertFalse(s.state.value.loading)
        s.clear(); assertEquals(AdminAuditState(),s.state.value)
    }
}
