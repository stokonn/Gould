package com.opus.piano.preferences

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Presentation-only state: cannot clear Room, journal notes, identity or notification consent. */
data class Appearance(
    val accent: String = "Gold", val cards: String = "Soft",
    val homeSections: List<String> = listOf("statistics","recent"), val hiddenHome: Set<String> = emptySet(),
    val metrics: List<String> = listOf("today","streak"),
    val profileSections: List<String> = listOf("statistics","activity","badges","frequent","history"), val hiddenProfile: Set<String> = emptySet(),
    val density: String = "Comfortable", val sort: String = "Recently practiced"
)
object AppearanceRules {
    val home = listOf("statistics","recent")
    val profile = listOf("statistics","activity","badges","frequent","history")
    val metrics = listOf("today","week","month","streak","sessions","pieces")
    val accents = listOf("Gold","Olive","Rose","Slate")
    val sorts = listOf("Recently practiced","Alphabetical","Composer","Recently added")
    fun order(raw: List<String>, allowed: List<String>) = (raw.filter { it in allowed }.distinct()+allowed).distinct()
    fun normalize(a: Appearance) = a.copy(accent=a.accent.takeIf { it in accents } ?: "Gold",cards=a.cards.takeIf { it in listOf("Soft","Outline","Open") } ?: "Soft",
        homeSections=order(a.homeSections,home),hiddenHome=a.hiddenHome.intersect(home.toSet()),metrics=order(a.metrics,metrics).take(2),
        profileSections=order(a.profileSections,profile),hiddenProfile=a.hiddenProfile.intersect(profile.toSet()),
        density=a.density.takeIf { it in listOf("Compact","Comfortable") } ?: "Comfortable",sort=a.sort.takeIf { it in sorts } ?: "Recently practiced")
    fun move(order: List<String>, id: String, delta: Int): List<String> {
        val index=order.indexOf(id);val target=index+delta
        return if (index<0 || target !in order.indices) order else order.toMutableList().apply { java.util.Collections.swap(this,index,target) }
    }
}
class AppearanceStore(context: Context) {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private val gate=Mutex()
    private val generation=java.util.concurrent.atomic.AtomicLong()
    private val prefs=context.getSharedPreferences("goldberg-appearance",Context.MODE_PRIVATE)
    private val mutable=MutableStateFlow(Appearance())
    val state=mutable.asStateFlow()
    private val loaded=scope.async { gate.withLock {
        fun list(key:String,default:List<String>)=prefs.getString(key,null)?.split(',') ?: default
        val d=Appearance()
        mutable.value=AppearanceRules.normalize(d.copy(accent=prefs.getString("accent",d.accent)!!,cards=prefs.getString("cards",d.cards)!!,
            homeSections=list("home",d.homeSections),hiddenHome=list("homeHidden",emptyList()).toSet(),metrics=list("metrics",d.metrics),
            profileSections=list("profile",d.profileSections),hiddenProfile=list("profileHidden",emptyList()).toSet(),
            density=prefs.getString("density",d.density)!!,sort=prefs.getString("sort",d.sort)!!))
    } }
    fun edit(change: (Appearance)->Appearance) { val expected=generation.get();scope.launch { loaded.await();gate.withLock {
        if(expected!=generation.get()) return@withLock
        val a=AppearanceRules.normalize(change(mutable.value))
        check(prefs.edit().putString("accent",a.accent).putString("cards",a.cards).putString("home",a.homeSections.joinToString(","))
            .putString("homeHidden",a.hiddenHome.joinToString(",")).putString("metrics",a.metrics.joinToString(","))
            .putString("profile",a.profileSections.joinToString(",")).putString("profileHidden",a.hiddenProfile.joinToString(","))
            .putString("density",a.density).putString("sort",a.sort).commit())
        mutable.value=a
    } } }
    suspend fun restore(a:Appearance) {
        generation.incrementAndGet();loaded.await();gate.withLock {
            com.opus.piano.data.backup.PortablePreferences.writeAppearance(prefs,a)
            mutable.value=a
        }
    }
    fun reset()=edit { Appearance() }
}
