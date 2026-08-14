package com.example.notesapp.data.voice

import android.content.Context
import android.content.pm.PackageManager
import com.example.notesapp.domain.voice.MicrophoneAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMicrophoneAvailability @Inject constructor(
    @ApplicationContext private val context: Context
) : MicrophoneAvailability {
    override fun isAvailable(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
}
