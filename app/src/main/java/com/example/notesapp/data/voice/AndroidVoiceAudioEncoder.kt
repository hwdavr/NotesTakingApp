package com.example.notesapp.data.voice

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import com.example.notesapp.domain.voice.AudioFormat
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceAudioEncoder @Inject constructor() : VoiceAudioEncoder {
    private var codec: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var muxerStarted = false
    private var trackIndex = -1
    private var presentationTimeUs = 0L
    private var bytesPerSecond = 1L
    private var stopped = true

    @Synchronized
    override fun start(outputPath: String, format: AudioFormat, config: VoiceAudioCaptureConfig) {
        release()
        require(config.channelCount == 1) { "Only mono voice capture is supported" }
        if (format == AudioFormat.OPUS && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            error("OPUS encoding requires Android Q or newer")
        }
        val mime = if (format == AudioFormat.OPUS) {
            MediaFormat.MIMETYPE_AUDIO_OPUS
        } else {
            MediaFormat.MIMETYPE_AUDIO_AAC
        }
        val muxerFormat = if (format == AudioFormat.OPUS) {
            MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG
        } else {
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        }
        val encoder = MediaCodec.createEncoderByType(mime)
        val mediaFormat = MediaFormat.createAudioFormat(
            mime,
            config.sampleRateHertz,
            config.channelCount
        ).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, if (format == AudioFormat.OPUS) 32_000 else 128_000)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, config.bufferSizeBytes.coerceAtLeast(4_096))
            if (format == AudioFormat.AAC) {
                setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
                )
            }
        }
        try {
            encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            codec = encoder
            muxer = MediaMuxer(outputPath, muxerFormat)
            muxerStarted = false
            trackIndex = -1
            presentationTimeUs = 0L
            bytesPerSecond = config.sampleRateHertz.toLong() * config.channelCount * 2L
            stopped = false
        } catch (error: Throwable) {
            encoder.runCatching { release() }
            release()
            throw error
        }
    }

    @Synchronized
    override fun writePcm(pcmBytes: ByteArray) {
        check(!stopped) { "Audio encoder is not started" }
        require(pcmBytes.isNotEmpty()) { "PCM frame must not be empty" }
        var offset = 0
        while (offset < pcmBytes.size) {
            val encoder = codec ?: error("Audio encoder is not available")
            drainOutput(encoder, endOfStream = false)
            val inputIndex = encoder.dequeueInputBuffer(INPUT_TIMEOUT_US)
            if (inputIndex >= 0) {
                val inputBuffer = encoder.getInputBuffer(inputIndex)
                if (inputBuffer != null) {
                    inputBuffer.clear()
                    val bytesToWrite = minOf(inputBuffer.remaining(), pcmBytes.size - offset)
                    if (bytesToWrite > 0) {
                        inputBuffer.put(pcmBytes, offset, bytesToWrite)
                        encoder.queueInputBuffer(
                            inputIndex,
                            0,
                            bytesToWrite,
                            presentationTimeUs,
                            0
                        )
                        presentationTimeUs += bytesToWrite * 1_000_000L / bytesPerSecond
                        offset += bytesToWrite
                    }
                }
            }
        }
    }

    @Synchronized
    override fun stop() {
        if (stopped) return
        val encoder = codec ?: run {
            release()
            return
        }
        try {
            var queuedEndOfStream = false
            while (!queuedEndOfStream) {
                val inputIndex = encoder.dequeueInputBuffer(INPUT_TIMEOUT_US)
                if (inputIndex >= 0) {
                    encoder.queueInputBuffer(
                        inputIndex,
                        0,
                        0,
                        presentationTimeUs,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    queuedEndOfStream = true
                } else {
                    drainOutput(encoder, endOfStream = false)
                }
            }
            drainOutput(encoder, endOfStream = true)
        } finally {
            release()
        }
    }

    @Synchronized
    private fun release() {
        codec?.runCatching { stop() }
        codec?.runCatching { release() }
        codec = null
        if (muxerStarted) muxer?.runCatching { stop() }
        muxer?.runCatching { release() }
        muxer = null
        muxerStarted = false
        trackIndex = -1
        stopped = true
    }

    private fun drainOutput(encoder: MediaCodec, endOfStream: Boolean) {
        val bufferInfo = MediaCodec.BufferInfo()
        var noOutputCount = 0
        while (noOutputCount < MAX_DRAIN_IDLE_POLLS) {
            when (val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, OUTPUT_TIMEOUT_US)) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    noOutputCount += 1
                    if (!endOfStream) return
                }

                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) error("Audio encoder changed output format twice")
                    val currentMuxer = muxer ?: error("Audio muxer is not available")
                    trackIndex = currentMuxer.addTrack(encoder.outputFormat)
                    currentMuxer.start()
                    muxerStarted = true
                    noOutputCount = 0
                }

                else -> {
                    if (outputIndex < 0) {
                        noOutputCount += 1
                        if (!endOfStream) return
                        continue
                    }
                    noOutputCount = 0
                    val outputBuffer = encoder.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                        writeSample(outputBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                }
            }
        }
        if (endOfStream) error("Audio encoder did not finish before timeout")
    }

    private fun writeSample(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        val currentMuxer = muxer ?: return
        outputBuffer.position(bufferInfo.offset)
        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
        currentMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
    }

    private companion object {
        const val INPUT_TIMEOUT_US = 10_000L
        const val OUTPUT_TIMEOUT_US = 10_000L
        const val MAX_DRAIN_IDLE_POLLS = 100
    }
}
