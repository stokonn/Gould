package com.opus.piano.data.backup

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.opus.piano.OpusApplication
import com.opus.piano.preferences.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

/** Explicit projection only. No identity/auth/quotes files, OS grants or network credentials. */
class PortablePreferences(private val context:Context,private val namespace:String="",private val live:Boolean=false) {
    private val appearance=context.getSharedPreferences(namespace+"goldberg-appearance",Context.MODE_PRIVATE)
    private val media=context.getSharedPreferences(namespace+"goldberg-media",Context.MODE_PRIVATE)
    private val local=context.getSharedPreferences(namespace+"goldberg-backup-settings",Context.MODE_PRIVATE)
    private val app get()=context.applicationContext as? OpusApplication
    suspend fun snapshot():JSONObject {
        val d=Appearance()
        fun list(k:String,default:List<String>)=appearance.getString(k,null)?.split(',') ?: default
        val a=AppearanceRules.normalize(d.copy(accent=appearance.getString("accent",d.accent)!!,cards=appearance.getString("cards",d.cards)!!,
            homeSections=list("home",d.homeSections),hiddenHome=list("homeHidden",emptyList()).toSet(),metrics=list("metrics",d.metrics),
            profileSections=list("profile",d.profileSections),hiddenProfile=list("profileHidden",emptyList()).toSet(),density=appearance.getString("density",d.density)!!,sort=appearance.getString("sort",d.sort)!!))
        val tag=local.getString("locale",null) ?: if(namespace.isEmpty()) withContext(Dispatchers.Main){AppCompatDelegate.getApplicationLocales().toLanguageTags()} else ""
        return JSONObject().put("appearance",encode(a)).put("locale",tag).put("previousMeteredConsent",media.getBoolean("metered-consent",false))
    }
    suspend fun apply(o:JSONObject) {
        val a=validate(o)
        if(live) requireNotNull(app).appearance.restore(a) else writeAppearance(appearance,a)
        check(media.edit().putBoolean("metered-consent",false).commit())
        if(live) requireNotNull(app).media.cellular.value=false
        check(local.edit().putString("locale",o.getString("locale")).commit())
        if(live) {
            app!!.identity.manualName();app!!.identity.manualAvatar()
            restoreDesiredLocale()
            com.opus.piano.reminders.ReminderScheduler(context).schedule()
        }
    }
    suspend fun restoreDesiredLocale() {
        local.getString("locale",null)?.let {tag->withContext(Dispatchers.Main){AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));com.opus.piano.i18n.L.refresh()}}
    }
    suspend fun selectLocale(tag:String) {
        require(validLocale(tag));withContext(Dispatchers.IO){check(local.edit().putString("locale",tag).commit())};restoreDesiredLocale()
    }
    companion object {
        fun validLocale(tag:String)=tag.isEmpty() || tag.matches(Regex("(?:cs|en)(?:-[A-Z]{2})?"))
        fun encode(a:Appearance)=JSONObject().put("accent",a.accent).put("cards",a.cards).put("homeSections",JSONArray(a.homeSections))
            .put("hiddenHome",JSONArray(a.hiddenHome.toList().sorted())).put("metrics",JSONArray(a.metrics)).put("profileSections",JSONArray(a.profileSections))
            .put("hiddenProfile",JSONArray(a.hiddenProfile.toList().sorted())).put("density",a.density).put("sort",a.sort)
        fun validate(o:JSONObject):Appearance {
            BackupJson.keys(o,setOf("appearance","locale","previousMeteredConsent"));require(o.get("previousMeteredConsent") is Boolean && validLocale(o.getString("locale")))
            val a=o.getJSONObject("appearance");BackupJson.keys(a,setOf("accent","cards","homeSections","hiddenHome","metrics","profileSections","hiddenProfile","density","sort"))
            fun list(key:String,allowed:List<String>,complete:Boolean=false):List<String> {
                val array=a.getJSONArray(key);require(array.length()<=allowed.size)
                val values=(0 until array.length()).map {array.getString(it)}
                require(values.distinct()==values && values.all {it in allowed} && (!complete || values.toSet()==allowed.toSet()));return values
            }
            require(a.getString("accent") in AppearanceRules.accents && a.getString("cards") in listOf("Soft","Outline","Open"))
            require(a.getString("density") in listOf("Compact","Comfortable") && a.getString("sort") in AppearanceRules.sorts)
            val metrics=list("metrics",AppearanceRules.metrics);require(metrics.size==2)
            return Appearance(a.getString("accent"),a.getString("cards"),list("homeSections",AppearanceRules.home,true),list("hiddenHome",AppearanceRules.home).toSet(),metrics,
                list("profileSections",AppearanceRules.profile,true),list("hiddenProfile",AppearanceRules.profile).toSet(),a.getString("density"),a.getString("sort"))
        }
        fun writeAppearance(p:android.content.SharedPreferences,a:Appearance) {
            check(p.edit().putString("accent",a.accent).putString("cards",a.cards).putString("home",a.homeSections.joinToString(",")).putString("homeHidden",a.hiddenHome.joinToString(","))
                .putString("metrics",a.metrics.joinToString(",")).putString("profile",a.profileSections.joinToString(",")).putString("profileHidden",a.hiddenProfile.joinToString(","))
                .putString("density",a.density).putString("sort",a.sort).commit())
        }
    }
}
