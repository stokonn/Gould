package com.opus.piano.data.offline

import android.content.Context

/**
 * SharedPreferences-backed [BundleJobPreferences] (`goldberg-bundle-jobs`).
 *
 * Kept in its own file so every other part of the job layer stays JVM-testable without
 * touching Android classes. `commit()` is used on purpose: a write that only lands in the
 * in-memory snapshot can be lost in exactly the process death this store exists to survive.
 */
class AndroidBundleJobPreferences(context: Context) : BundleJobPreferences {
    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(key: String, value: String?) {
        val editor = prefs.edit()
        if (value == null) editor.remove(key) else editor.putString(key, value)
        check(editor.commit()) { "Failed to persist bundle job state" }
    }

    override fun keys(): Set<String> = prefs.all.keys.toSet()

    companion object {
        const val FILE_NAME = "goldberg-bundle-jobs"
    }
}
