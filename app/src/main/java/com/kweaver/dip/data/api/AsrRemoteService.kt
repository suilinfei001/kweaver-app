package com.kweaver.dip.data.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsrRemoteService @Inject constructor(
    private val client: OkHttpClient,
    private val context: Context,
) {
    companion object {
        private const val TAG = "AsrRemoteService"
        private const val SAMPLE_RATE = 16000
    }

    private var baseUrl: String = ""

    fun configure(url: String) {
        baseUrl = url.trimEnd('/')
    }

    suspend fun recognize(audioBytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("ASR URL not configured"))
        }

        try {
            val url = "$baseUrl/asr/raw?sample_rate=$SAMPLE_RATE"
            Log.d(TAG, "=== ASR Request ===")
            Log.d(TAG, "URL: $url")
            Log.d(TAG, "Audio size: ${audioBytes.size} bytes")

            val audioDir = File(context.getExternalFilesDir(null), "asr_audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            val audioFile = File(audioDir, "recording_${System.currentTimeMillis()}.pcm")
            audioFile.writeBytes(audioBytes)
            Log.d(TAG, "Audio saved to: ${audioFile.absolutePath}")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody("audio/pcm".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {
                    Log.e(TAG, "ASR Response error: ${response.code}")
                    return@withContext Result.failure(
                        java.io.IOException("ASR request failed: ${response.code}")
                    )
                }

                val body = response.body?.string()
                Log.d(TAG, "ASR Response: $body")

                if (body.isNullOrBlank()) {
                    return@withContext Result.success("")
                }

                val result = parseResponse(body)
                if (result.isSuccess) {
                    Result.success(result.getOrDefault(""))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ASR error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseResponse(responseBody: String): Result<String> {
        return try {
            val json = org.json.JSONObject(responseBody)
            val success = json.optBoolean("success", false)
            val error = json.optString("error", null)

            if (!success && error != null) {
                return Result.failure(Exception(error))
            }

            val text = json.optString("text", "")
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}