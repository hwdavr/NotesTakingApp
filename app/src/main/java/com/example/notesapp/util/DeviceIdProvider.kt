package com.example.notesapp.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("device_identity", Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        preferences.getString("device_id", null) ?: run {
            val value = "android_${UUID.randomUUID()}"
            preferences.edit().putString("device_id", value).apply()
            value
        }
    }
}
