package com.kweaver.dip.ui.screens.chat

import android.util.Log
import com.kweaver.dip.data.api.AsrRemoteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VoiceRecognitionManager(
    private val asrRemoteService: AsrRemoteService,
) {
    companion object {
        private const val TAG = "VoiceRecognitionManager"
        const val SAMPLE_RATE = 16000
    }

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private var audioBuffer = mutableListOf<Short>()

    fun processAudioChunk(audioData: ByteArray) {
        val byteBuffer = ByteBuffer.wrap(audioData)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()

        val shorts = ShortArray(byteBuffer.remaining())
        byteBuffer.get(shorts)

        audioBuffer.addAll(shorts.toList())
        Log.d(TAG, "processAudioChunk: added ${audioData.size} bytes, total buffer: ${audioBuffer.size} shorts")

        if (audioBuffer.size > SAMPLE_RATE * 60) {
            audioBuffer = audioBuffer.takeLast(SAMPLE_RATE * 30).toMutableList()
        }
    }

    fun startRecording() {
        _isRecording.value = true
        clearBuffer()
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun getAccumulatedAudioBytes(): ByteArray {
        val shortArray = audioBuffer.toShortArray()
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asShortBuffer().put(shortArray)
        return byteBuffer.array()
    }

    suspend fun recognize(): Result<String> {
        if (audioBuffer.isEmpty()) {
            return Result.success("")
        }

        _isRecognizing.value = true
        val audioBytes = getAccumulatedAudioBytes()
        val result = asrRemoteService.recognize(audioBytes)
        _isRecognizing.value = false

        result.onSuccess { text ->
            _recognizedText.value = text
        }

        return result
    }

    fun clearBuffer() {
        audioBuffer.clear()
        _recognizedText.value = ""
    }

    fun release() {
        clearBuffer()
    }
}