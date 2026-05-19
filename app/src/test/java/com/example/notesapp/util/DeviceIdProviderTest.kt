package com.example.notesapp.util

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeviceIdProviderTest {
    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    @Before
    fun setup() {
        every { context.getSharedPreferences("device_identity", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
    }

    @Test
    fun `deviceId returns existing id if present`() {
        every { prefs.getString("device_id", null) } returns "existing-id"

        val provider = DeviceIdProvider(context)
        assertEquals("existing-id", provider.deviceId)

        // Verify no new ID was generated
        verify(exactly = 0) { editor.putString(any(), any()) }
    }

    @Test
    fun `deviceId generates and saves new id if not present`() {
        every { prefs.getString("device_id", null) } returns null

        val provider = DeviceIdProvider(context)
        val id = provider.deviceId

        assertTrue(id.startsWith("android_"))
        verify { editor.putString("device_id", id) }
        verify { editor.apply() }
    }
}
