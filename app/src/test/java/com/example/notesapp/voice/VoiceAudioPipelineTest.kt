package com.example.notesapp.voice

import android.os.ParcelFileDescriptor
import com.example.notesapp.data.voice.PcmAudioSource
import com.example.notesapp.data.voice.TranscriptAudioSourceRegistry
import com.example.notesapp.data.voice.voiceAudioCaptureConfigForFormat
import com.example.notesapp.domain.voice.AudioFormat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoiceAudioPipelineTest {
    @Test
    fun `capture config preserves product sample rates`() {
        assertEquals(44_100, voiceAudioCaptureConfigForFormat(AudioFormat.AAC).sampleRateHertz)
        assertEquals(16_000, voiceAudioCaptureConfigForFormat(AudioFormat.OPUS).sampleRateHertz)
    }

    @Test
    fun `pcm source forwards frames only after recognizer attaches`() {
        val source = PcmAudioSource(sampleRateHertz = 16_000)
        val descriptor = source.attachForRecognizer()
        checkNotNull(descriptor)
        val input = ParcelFileDescriptor.AutoCloseInputStream(descriptor)
        val expected = byteArrayOf(1, 2, 3, 4)

        source.write(expected)

        val actual = ByteArray(expected.size)
        assertEquals(expected.size, input.read(actual))
        assertArrayEquals(expected, actual)

        source.disable()
        assertEquals(-1, input.read())
        source.close()
    }

    @Test
    fun `pcm source ignores frames before attach and after close`() {
        val source = PcmAudioSource(sampleRateHertz = 16_000)

        source.write(byteArrayOf(1, 2, 3, 4))
        source.close()

        assertEquals(null, source.attachForRecognizer())
        source.write(byteArrayOf(5, 6, 7, 8))
    }

    @Test
    fun `registry closes a replaced source`() {
        val registry = TranscriptAudioSourceRegistry()
        val first = PcmAudioSource(sampleRateHertz = 16_000)
        val second = PcmAudioSource(sampleRateHertz = 16_000)

        registry.register("session", first)
        registry.register("session", second)

        assertEquals(second, registry.find("session"))
        assertEquals(null, first.attachForRecognizer())
        registry.remove("session")
        assertEquals(null, registry.find("session"))
    }
}
