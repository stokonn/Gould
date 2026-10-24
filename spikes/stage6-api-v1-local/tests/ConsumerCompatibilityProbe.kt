package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Actual unchanged Kotlin client compiled separately; captured HTTP replies only.
 * No Android runtime, network socket, Gradle task, app storage or worktree write. */
fun main(args: Array<String>) = runBlocking {
    val captures = JSONObject(File(args[0]).readText()).getJSONArray("captures")
    val byId = (0 until captures.length()).associate { val x=captures.getJSONObject(it); x.getString("id") to x }
    val cases=JSONObject(File(args[1]).readText()).getJSONArray("cases")
    val out=JSONArray()
    for (i in 0 until cases.length()) {
        val case=cases.getJSONObject(i)
        val capture=byId[case.optString("fixtureId")]
        var calls=0
        var token="synthetic-token"
        val transport=AdminTransport { _, url, _, _ ->
            calls++
            if(case.getString("operation")=="account-switch")token="other-account-token"
            val selected=if(case.getString("operation")=="stats" && url.substringBefore('?').endsWith("/admin/audit")) byId["audit.first"] else capture
            check(selected!=null)
            AdminHttpResponse(selected.getInt("status"),if(selected.isNull("response"))ByteArray(0) else selected.getJSONObject("response").toString().toByteArray(),null)
        }
        val api=AdminApi({"https://synthetic.invalid"},transport,{token})
        val operation=case.getString("operation")
        val actual=when(operation) {
            "capabilities","account-switch" -> {
                val result=api.capabilities()
                if(result is AdminResult.Ok){
                    val expected=capture!!.getJSONObject("response")
                    check(result.value.principalId==expected.getString("principalId"))
                    val caps=expected.getJSONArray("capabilities")
                    check(result.value.capabilities==(0 until caps.length()).map{caps.getString(it)}.toSet())
                }
                outcome(result)
            }
            "queue" -> outcome(api.queue(null,null))
            "detail" -> outcome(api.submission(capture!!.getJSONObject("response").getString("submissionId")))
            "decision" -> {
                val result=api.decide("synthetic-id",JSONObject())
                if(case.getString("id")=="conflict"){
                    check(result is AdminResult.Err && result.failure is AdminFailure.Conflict)
                    check(result.failure.currentRevision==null && result.failure.currentStatus==null)
                    check(calls==1)
                }
                outcome(result)
            }
            "audit" -> outcome(api.audit(null))
            "logout" -> outcome(api.signOut())
            "stats" -> if(AdminStatsService(api).load(0).allExact) "stats_exact" else "stats_unavailable"
            "cursor" -> {
                val cursor=capture!!.getJSONObject("response").getString("nextCursor")
                check(AdminWireValues.cursor(JSONObject().put("nextCursor",cursor))==cursor)
                "exact_cursor"
            }
            "revision-max" -> try {
                AdminWireValues.revision(JSONObject().put("revision",capture!!.getJSONObject("response").getString("publishedEntityRevision")))
                "accepted"
            } catch (_:IllegalArgumentException) { "rejected" }
            "long-max" -> {
                val value=capture!!.getJSONObject("response").getString("publishedEntityRevision")
                check(AdminWireValues.unsignedLong(JSONObject().put("value",value),"value").toString()==value)
                "exact_int8"
            }
            "disabled" -> outcome(AdminApi.forApplication({"https://synthetic.invalid"},{token}).capabilities())
            "legacy-body" -> {
                val b=buildDecisionBody(AdminDecision.APPROVE,1,null,"synthetic-key")
                check(b.has("expectedRevision")&&!b.has("expectedEntityRevision")&&!b.has("expectedSubmissionRevision"))
                "single_numeric_revision"
            }
            else -> error("Unknown probe operation")
        }
        check(actual==case.getString("expectedOutcome")) { "${case.getString("id")}: $actual" }
        out.put(JSONObject().put("id",case.getString("id")).put("actualOutcome",actual)
            .put("classification",case.getString("classification")).put("transportCalls",calls))
    }
    println(JSONObject().put("executedCases",out.length()).put("cases",out)
        .put("mechanism","unmodified selected Kotlin JVM API/DTO code replaying real HTTP/PG captures; no Android runtime").toString())
}
private fun outcome(result: AdminResult<*>): String = when(result) {
    is AdminResult.Ok -> "ok"
    is AdminResult.Err -> result.failure.key
}
