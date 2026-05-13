package com.kweaver.dip.data.api

import android.util.Log
import com.google.gson.Gson
import com.kweaver.dip.data.model.ChatMessageDto
import com.kweaver.dip.data.model.ChatRequest
import com.kweaver.dip.data.model.ChatResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class SseChatService @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    companion object {
        private const val TAG = "SseChatService"
        private val SSE_MEDIA_TYPE = "text/event-stream".toMediaType()
    }

    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        contextSize: Int = 4096,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): Flow<String> = callbackFlow {
        val url = "${baseUrl.trimEnd('/')}/chat/completions"
        Log.d(TAG, "=== SSE Connection Start ===")
        Log.d(TAG, "URL: $url")
        Log.d(TAG, "Model: $model")
        Log.d(TAG, "Messages count: ${messages.size}")

        val requestBody = gson.toJson(ChatRequest(model = model, messages = messages))
        Log.d(TAG, "Request body: $requestBody")

        val requestBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Accept", "text/event-stream; charset=utf-8")
        if (apiKey.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }
        val request = requestBuilder
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        var chunkCount = 0

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e(TAG, "SSE Request failed: ${e.message}", e)
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "SSE Response unsuccessful: ${response.code}")
                    close(java.io.IOException("SSE response error: ${response.code}"))
                    return
                }

                Log.d(TAG, "SSE Response received, code: ${response.code}")

                val body = response.body
                if (body == null) {
                    Log.e(TAG, "SSE Response body is null")
                    close(java.io.IOException("SSE response body is null"))
                    return
                }

                val source = body.source()
                val buffer = okio.Buffer()

                try {
                    while (true) {
                        val line = source.readUtf8Line() ?: break
                        Log.v(TAG, "SSE Raw line: $line")

                        if (line.startsWith("data:")) {
                            val data = line.substringAfter("data:").trim()
                            Log.d(TAG, "SSE Data: $data")

                            if (data == "[DONE]") {
                                Log.d(TAG, "SSE Received [DONE], completing")
                                close()
                                break
                            }

                            try {
                                val chatResponse = gson.fromJson(data, ChatResponse::class.java)
                                val content = chatResponse.choices.firstOrNull()?.delta?.content
                                if (content != null) {
                                    chunkCount++
                                    Log.v(TAG, "SSE Chunk #$chunkCount: '$content'")
                                    trySend(content)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "SSE Parse error: ${e.message}, data: $data")
                            }
                        }
                    }
                    Log.d(TAG, "SSE Stream completed. Total chunks: $chunkCount")
                    close()
                } catch (e: Exception) {
                    Log.e(TAG, "SSE Stream error: ${e.message}", e)
                    close(e)
                } finally {
                    body.close()
                }
            }
        })

        awaitClose {
            Log.d(TAG, "SSE Flow cancelled, cancelling call...")
            call.cancel()
        }
    }.flowOn(coroutineContext)
}