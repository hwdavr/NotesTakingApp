package com.example.notesapp.data.voice

import android.content.Context
import android.os.StatFs
import com.example.notesapp.domain.voice.StorageInfoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidStorageInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : StorageInfoProvider {
    override fun availableBytes(): Long = StatFs(context.filesDir.path).availableBytes
}
