package com.kweaver.dip.data.api

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

data class TtsResponse(
    @SerializedName("audio_base64")
    val audioBase64: String?
)

@Singleton
class TtsRemoteService @Inject constructor(
    private val client: OkHttpClient,
) {
    companion object {
        private const val TAG = "TtsRemoteService"
        private const val SAMPLE_RATE = 22050
    }

    private var baseUrl: String = ""
    private var audioTrack: AudioTrack? = null
    private val gson = Gson()

    fun configure(url: String) {
        baseUrl = url.trimEnd('/')
    }

    private fun parseJsonResponse(json: String): ByteArray? {
        return try {
            val response = gson.fromJson(json, TtsResponse::class.java)
            val base64 = response?.audioBase64
            if (base64.isNullOrBlank()) {
                Log.w(TAG, "No audio_base64 in response")
                return null
            }
            android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
            null
        }
    }

    suspend fun synthesize(text: String, speed: Float = 1.0f): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("TTS URL not configured"))
        }

        if (text.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty"))
        }

        try {
            val url = "$baseUrl/tts"
            Log.d(TAG, "=== TTS Request ===")
            Log.d(TAG, "URL: $url")
            Log.d(TAG, "Text: $text (length: ${text.length})")
            Log.d(TAG, "Speed: $speed")

            val requestJson = mapOf(
                "text" to text,
                "speed" to speed
            )
            val jsonBody = gson.toJson(requestJson)
            Log.d(TAG, "JSON Body: $jsonBody")

            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "TTS Response error: ${response.code}")
                    return@withContext Result.failure(
                        java.io.IOException("TTS request failed: ${response.code}")
                    )
                }

                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Empty response from TTS service"))
                }

                Log.d(TAG, "TTS Response: $body")

                val audioBytes = parseJsonResponse(body)
                if (audioBytes != null) {
                    val pcmBytes = if (isWavFormat(audioBytes)) {
                        extractPcmFromWav(audioBytes) ?: audioBytes
                    } else {
                        audioBytes
                    }
                    Log.d(TAG, "Decoded audio: ${pcmBytes.size} bytes (WAV extracted)")
                    Result.success(pcmBytes)
                } else {
                    Result.failure(Exception("Failed to decode audio"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun isWavFormat(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        return bytes[0] == 'R'.code.toByte() &&
               bytes[1] == 'I'.code.toByte() &&
               bytes[2] == 'F'.code.toByte() &&
               bytes[3] == 'F'.code.toByte() &&
               bytes[8] == 'W'.code.toByte() &&
               bytes[9] == 'A'.code.toByte() &&
               bytes[10] == 'V'.code.toByte() &&
               bytes[11] == 'E'.code.toByte()
    }

    private fun extractPcmFromWav(wavBytes: ByteArray): ByteArray? {
        return try {
            if (wavBytes.size < 44) return null

            var offset = 12
            var dataOffset = -1
            var dataSize = 0

            while (offset < wavBytes.size - 8) {
                val chunkId = String(byteArrayOf(
                    wavBytes[offset],
                    wavBytes[offset + 1],
                    wavBytes[offset + 2],
                    wavBytes[offset + 3]
                ))
                val chunkSize = wavBytes[offset + 4].toInt() and 0xFF or
                               (wavBytes[offset + 5].toInt() and 0xFF shl 8) or
                               (wavBytes[offset + 6].toInt() and 0xFF shl 16) or
                               (wavBytes[offset + 7].toInt() and 0xFF shl 24)

                if (chunkId == "data") {
                    dataOffset = offset + 8
                    dataSize = chunkSize
                    break
                }

                offset += 8 + chunkSize
                if (chunkSize % 2 != 0) offset += 1
            }

            if (dataOffset == -1 || dataSize == 0) {
                Log.w(TAG, "No data chunk found, using fallback")
                if (wavBytes.size > 44) {
                    dataOffset = 44
                    dataSize = wavBytes.size - 44
                } else {
                    return null
                }
            }

            val pcmBytes = ByteArray(minOf(dataSize, wavBytes.size - dataOffset))
            System.arraycopy(wavBytes, dataOffset, pcmBytes, 0, pcmBytes.size)
            Log.d(TAG, "Extracted PCM: ${pcmBytes.size} bytes from offset $dataOffset")
            pcmBytes
        } catch (e: Exception) {
            Log.e(TAG, "WAV extract error: ${e.message}", e)
            null
        }
    }

    fun initAudioTrack() {
        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        Log.d(TAG, "AudioTrack initialized, buffer size: $bufferSize")
    }

    fun playAudio(audioBytes: ByteArray) {
        try {
            if (audioTrack == null) {
                initAudioTrack()
            }
            audioTrack?.write(audioBytes, 0, audioBytes.size)
            Log.d(TAG, "Played ${audioBytes.size} bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Play error: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
            Log.d(TAG, "AudioTrack stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Stop error: ${e.message}", e)
        }
    }
}