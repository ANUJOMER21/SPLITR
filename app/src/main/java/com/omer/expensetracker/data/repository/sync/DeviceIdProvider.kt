package com.omer.expensetracker.data.repository.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A random id stable for this install, distinct from the signed-in account's uid. The same
 * Google account can be signed in on several phones — tagging pushed documents with just the
 * uid can't tell "my other device wrote this" apart from "this device is seeing its own echo",
 * so every push is tagged with this instead, and the pull listener only skips a change if it
 * carries this exact device's id.
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("device_identity", Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }
    }

    private companion object {
        const val KEY = "device_id"
    }
}
