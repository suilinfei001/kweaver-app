package com.kweaver.dip.data.api

import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.SseEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenDataStore: TokenDataStore
) {
    fun streamChat(
        serverUrl: String,
        sessionKey: String,
        userId: String,
        message: String,
        attachments: List<Pair<String, String>>? = null
    ): Flow<SseEvent> = callbackFlow {
        val token = tokenDataStore.getAccessToken()

        val jsonBody = buildString {
            append("{\"input\":")
            append(com.google.gson.Gson().toJson(message))
            if (!attachments.isNullOrEmpty()) {
                append(",\"attachments\":[")
                attachments.forEachIndexed { index, (name, path) ->
                    if (index > 0) append(",")
                    append("{\"name\":\"$name\",\"path\":\"$path\"}")
                }
                append("]")
            }
            append("}")
        }

        val request = Request.Builder()
            .url("$serverUrl/api/dip-studio/v1/chat/agent")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer ${token ?: ""}")
            .addHeader("Token", token ?: "")
            .addHeader("x-openclaw-session-key", sessionKey)
            .addHeader("x-user-id", userId)
            .addHeader("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")
            .build()

        val call = okHttpClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trySend(SseEvent("error", e.message ?: "Connection failed"))
                close(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    trySend(SseEvent("error", "HTTP ${response.code}: ${response.message}"))
                    close()
                    return
                }

                val reader = response.body?.byteStream()?.bufferedReader()
                if (reader == null) {
                    trySend(SseEvent("error", "Empty response body"))
                    close()
                    return
                }

                try {
                    var currentEvent = ""
                    val currentData = StringBuilder()

                    reader.use { r ->
                        var line = r.readLine()
                        while (line != null) {
                            when {
                                line.startsWith("event:") -> {
                                    currentEvent = line.removePrefix("event:").trim()
                                }
                                line.startsWith("data:") -> {
                                    currentData.appendLine(line.removePrefix("data:").trim())
                                }
                                line.isBlank() && currentData.isNotEmpty() -> {
                                    trySend(
                                        SseEvent(currentEvent, currentData.toString().trimEnd())
                                    )
                                    currentEvent = ""
                                    currentData.clear()
                                }
                            }
                            line = r.readLine()
                        }

                        if (currentData.isNotEmpty()) {
                            trySend(SseEvent(currentEvent, currentData.toString().trimEnd()))
                        }

                        trySend(SseEvent("done", ""))
                    }
                } catch (e: Exception) {
                    trySend(SseEvent("error", e.message ?: "Stream read error"))
                } finally {
                    close()
                }
            }
        })

        awaitClose { call.cancel() }
    }
}
