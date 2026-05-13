package com.kweaver.dip.data.tts

import android.util.Log
import com.kweaver.dip.data.api.TtsRemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TtsPlayer(
    private val ttsService: TtsRemoteService,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "TtsPlayer"
        private val SENTENCE_DELIMITERS = setOf('。', '！', '？', '.', '!', '?', '\n')
    }

    private val mutex = Mutex()
    private var pendingText = StringBuilder()
    private var currentJob: Job? = null
    private var isPlaying = false

    var onPlaybackStart: (() -> Unit)? = null
    var onPlaybackEnd: (() -> Unit)? = null

    fun onNewToken(token: String) {
        scope.launch {
            pendingText.append(token)

            val sentence = extractSentence()
            if (sentence != null) {
                synthesizeAndPlay(sentence)
            }
        }
    }

    private fun extractSentence(): String? {
        val text = pendingText.toString()

        for (i in text.length - 1 downTo 0) {
            if (SENTENCE_DELIMITERS.contains(text[i])) {
                val sentence = text.substring(0, i + 1)
                pendingText.clear()
                pendingText.append(text.substring(i + 1))
                return sentence
            }
        }

        if (pendingText.length > 50) {
            val text = pendingText.toString()
            val lastSpace = text.lastIndexOf(' ')
            if (lastSpace > 20) {
                val sentence = text.substring(0, lastSpace)
                pendingText.clear()
                pendingText.append(text.substring(lastSpace + 1))
                return sentence
            }
        }

        return null
    }

    private suspend fun synthesizeAndPlay(sentence: String) {
        mutex.withLock {
            if (isPlaying) {
                currentJob?.cancel()
            }

            isPlaying = true
            onPlaybackStart?.invoke()

            val result = ttsService.synthesize(sentence)
            result.onSuccess { audioBytes ->
                withContext(Dispatchers.Main) {
                    ttsService.playAudio(audioBytes)
                }
            }.onFailure { e ->
                Log.e(TAG, "TTS synthesis failed: ${e.message}")
            }

            isPlaying = false
            onPlaybackEnd?.invoke()
        }
    }

    fun flush() {
        scope.launch {
            val remaining = pendingText.toString()
            if (remaining.isNotBlank()) {
                pendingText.clear()
                synthesizeAndPlay(remaining)
            }
        }
    }

    fun stop() {
        currentJob?.cancel()
        ttsService.stop()
        pendingText.clear()
        isPlaying = false
    }
}