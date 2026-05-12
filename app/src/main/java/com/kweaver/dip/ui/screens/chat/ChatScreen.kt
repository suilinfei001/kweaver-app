package com.kweaver.dip.ui.screens.chat

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kweaver.dip.data.api.AsrRemoteService
import com.kweaver.dip.ui.components.MessageBubble
import com.kweaver.dip.ui.components.StreamingBubble
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasRecordPermission by remember { mutableStateOf(false) }
    var isVoiceMode by remember { mutableStateOf(false) }

    val audioCapturer = remember { AudioCapturer(context) }
    val asrRemoteService = remember { AsrRemoteService(OkHttpClient(), context) }
    val voiceRecognizer = remember { VoiceRecognitionManager(asrRemoteService) }

    var recordingJob by remember { mutableStateOf<Job?>(null) }
    val recognizedText by voiceRecognizer.recognizedText.collectAsState()
    val isRecording by voiceRecognizer.isRecording.collectAsState()
    val isRecognizing by voiceRecognizer.isRecognizing.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (!isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("需要麦克风权限才能使用语音识别")
            }
        }
    }

    LaunchedEffect(uiState.config.asrEnabled, uiState.config.asrUrl) {
        if (uiState.config.asrEnabled && uiState.config.asrUrl.isNotBlank()) {
            asrRemoteService.configure(uiState.config.asrUrl)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recordingJob?.cancel()
            voiceRecognizer.release()
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        val totalItems = uiState.messages.size + if (uiState.streamingContent.isNotEmpty()) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 对话") },
                actions = {
                    IconButton(onClick = viewModel::newConversation) {
                        Icon(Icons.Default.Add, contentDescription = "新对话")
                    }
                    DebugVariantButton()
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = padding,
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id },
                ) { message ->
                    MessageBubble(message = message)
                }

                if (uiState.streamingContent.isNotEmpty()) {
                    item {
                        StreamingBubble(content = uiState.streamingContent)
                    }
                }
            }

            if (isVoiceMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "相机",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    var showHint by remember { mutableStateOf(false) }
                    var pressStartTime by remember { mutableStateOf(0L) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                color = if (isRecording)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(24.dp),
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        Log.d("ChatScreen", "=== onPress started ===")
                                        if (!hasRecordPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            tryAwaitRelease()
                                            return@detectTapGestures
                                        }

                                        if (!uiState.config.asrEnabled || uiState.config.asrUrl.isBlank()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("请先在设置中配置 ASR 服务")
                                            }
                                            tryAwaitRelease()
                                            return@detectTapGestures
                                        }

                                        pressStartTime = System.currentTimeMillis()
                                        showHint = false

                                        val hintJob = scope.launch {
                                            delay(1000)
                                            showHint = true
                                        }

                                        voiceRecognizer.startRecording()
                                        recordingJob = scope.launch {
                                            audioCapturer.startRecording().collect { audioChunk ->
                                                voiceRecognizer.processAudioChunk(audioChunk)
                                            }
                                        }

                                        tryAwaitRelease()
                                        hintJob.cancel()

                                        val pressDuration = System.currentTimeMillis() - pressStartTime
                                        Log.d("ChatScreen", "=== onPress ended, duration: $pressDuration ===")

                                        if (pressDuration < 500) {
                                            showHint = false
                                        }

                                        recordingJob?.cancel()
                                        voiceRecognizer.stopRecording()

                                        scope.launch {
                                            val result = voiceRecognizer.recognize()
                                            result.onSuccess { finalText ->
                                                Log.d("ChatScreen", "=== recognized text: $finalText ===")
                                                if (finalText.isNotBlank()) {
                                                    viewModel.onSpeechResult(finalText)
                                                } else {
                                                    snackbarHostState.showSnackbar("未识别到文字，请重试")
                                                }
                                            }.onFailure { e ->
                                                Log.e("ChatScreen", "=== ASR error: ${e.message} ===")
                                                snackbarHostState.showSnackbar("识别失败: ${e.message}")
                                            }
                                            voiceRecognizer.clearBuffer()
                                        }
                                    },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when {
                                isRecording -> "识别中..."
                                showHint -> "请长按按钮开始说话"
                                else -> "按住说话"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }

                    IconButton(onClick = { isVoiceMode = false }) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "键盘",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        maxLines = 4,
                        enabled = !uiState.isStreaming,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { isVoiceMode = true },
                        enabled = !uiState.isStreaming,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "语音输入",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = uiState.inputText.isNotBlank() && !uiState.isStreaming,
                    ) {
                        Text(
                            text = "发送",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.inputText.isNotBlank() && !uiState.isStreaming)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugVariantButton() {
    var showMenu by remember { mutableStateOf(false) }
    var currentVariant by remember { mutableStateOf(0) }
    val variants = listOf("原版", "A - 极简", "B - 卡片", "C - 沉浸")

    Box {
        IconButton(onClick = { showMenu = true }) {
            Text(
                text = "D${currentVariant + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            Text(
                text = "设计风格切换",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            variants.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        currentVariant = index
                        showMenu = false
                    },
                    leadingIcon = {
                        if (currentVariant == index) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }
    }
}