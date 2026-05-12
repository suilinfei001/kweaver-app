package com.kweaver.dip.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class AudioCapturer(private val context: Context) {

    companion object {
        private const val TAG = "AudioCapturer"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    )

    init {
        Log.d(TAG, "=== AudioCapturer init ===")
        Log.d(TAG, "Sample rate: $SAMPLE_RATE")
        Log.d(TAG, "Channel config: $CHANNEL_CONFIG")
        Log.d(TAG, "Audio format: $AUDIO_FORMAT")
        Log.d(TAG, "Buffer size: $bufferSize")
        Log.d(TAG, "Min buffer size: ${AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)}")
    }

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording(): Flow<ByteArray> = flow {
        if (!hasRecordPermission()) {
            throw IllegalStateException("RECORD_AUDIO permission not granted")
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            throw IllegalStateException("AudioRecord initialization failed")
        }

        audioRecord.startRecording()
        Log.d(TAG, "=== Recording started ===")

        try {
            val buffer = ByteArray(bufferSize)
            var totalBytesRead = 0
            while (coroutineContext.isActive) {
                val bytesRead = audioRecord.read(buffer, 0, bufferSize)
                if (bytesRead > 0) {
                    totalBytesRead += bytesRead
                    Log.d(TAG, "bytesRead: $bytesRead, total: $totalBytesRead, first4bytes: ${buffer[0]},${buffer[1]},${buffer[2]},${buffer[3]}")
                    emit(buffer.copyOf(bytesRead))
                }
            }
            Log.d(TAG, "=== Recording loop ended, total bytes: $totalBytesRead ===")
        } finally {
            audioRecord.stop()
            audioRecord.release()
            Log.d(TAG, "=== Recording stopped ===")
        }
    }.flowOn(Dispatchers.IO)
}