package com.kweaver.dip.data.api

import com.google.gson.Gson
import com.kweaver.dip.data.model.ChatMessageDto
import com.kweaver.dip.data.model.ChatRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class SseChatService @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        contextSize: Int = 4096,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): Flow<String> = callbackFlow {
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .post(
                gson.toJson(ChatRequest(model = model, messages = messages))
                    .toRequestBody("application/json".toMediaType())
            )
            .build()

        val factory = EventSources.createFactory(client)
        val eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                try {
                    val response = gson.fromJson(data, com.kweaver.dip.data.model.ChatResponse::class.java)
                    val content = response.choices.firstOrNull()?.delta?.content
                    if (content != null) {
                        trySend(content)
                    }
                } catch (_: Exception) {
                    // Malformed JSON chunk, skip
                }
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                close(t ?: Exception("SSE connection failed"))
            }
        })

        awaitClose {
            eventSource.cancel()
        }
    }.flowOn(coroutineContext)
}
