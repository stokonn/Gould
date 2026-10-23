package com.opus.piano.data.community

import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Cache contains only explicitly selected public metadata, never credentials or drafts. */
class CommunityOverlayCache(private val file: File) {
    fun read(): OverlaySnapshot?=runCatching {
        val o=JSONObject(file.readText());OverlaySnapshot(CommunityCodec.overlay(o.getJSONObject("page").toString()),o.getLong("at"),true,o.getBoolean("synthetic"))
    }.getOrNull()
    fun write(snapshot: OverlaySnapshot) {
        val items=JSONArray();snapshot.page.items.forEach {v ->
            items.put(JSONObject().put("kind",v.kind.wire).put("catalogueUuid",v.catalogueUuid.value).put("displayTitle",v.displayTitle).put("revision",v.revision.value)
                .put("provenanceSummary",JSONObject().put("licenses",JSONArray(v.provenanceSummary.licenses)).put("sourceCount",v.provenanceSummary.sourceCount)))
        }
        val o=JSONObject().put("at",snapshot.synchronizedAt).put("synthetic",snapshot.synthetic).put("page",JSONObject().put("items",items).put("nextCursor",snapshot.page.nextCursor?:JSONObject.NULL))
        check(file.parentFile!!.isDirectory||file.parentFile!!.mkdirs());val tmp=File.createTempFile(".overlay-",".tmp",file.parentFile)
        try {FileOutputStream(tmp).use {it.write(o.toString().toByteArray());it.fd.sync()};Files.move(tmp.toPath(),file.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)}finally {tmp.delete()}
    }
}
