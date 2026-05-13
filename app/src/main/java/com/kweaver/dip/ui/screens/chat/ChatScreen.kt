package com.kweaver.dip.ui.screens.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kweaver.dip.data.api.AsrRemoteService
import com.kweaver.dip.ui.components.MessageBubble
import com.kweaver.dip.ui.components.StreamingBubble
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
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
    val isRecording by voiceRecognizer.isRecording.collectAsState()
    var showHint by remember { mutableStateOf(false) }

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
                title = { Text("AI 对话", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = viewModel::newConversation) {
                        Icon(Icons.Default.Add, contentDescription = "新对话")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                VoiceInputBar(
                    isRecording = isRecording,
                    showHint = showHint,
                    onShowHintChange = { showHint = it },
                    onPressStart = {
                        if (!hasRecordPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@VoiceInputBar
                        }
                        if (!uiState.config.asrEnabled || uiState.config.asrUrl.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请先在设置中配置 ASR 服务")
                            }
                            return@VoiceInputBar
                        }
                        voiceRecognizer.startRecording()
                        recordingJob = scope.launch {
                            audioCapturer.startRecording().collect { audioChunk ->
                                voiceRecognizer.processAudioChunk(audioChunk)
                            }
                        }
                    },
                    onPressEnd = { duration ->
                        recordingJob?.cancel()
                        voiceRecognizer.stopRecording()
                        if (duration < 500) {
                            showHint = false
                            return@VoiceInputBar
                        }
                        scope.launch {
                            val result = voiceRecognizer.recognize()
                            result.onSuccess { finalText ->
                                if (finalText.isNotBlank()) {
                                    viewModel.onSpeechResult(finalText)
                                } else {
                                    snackbarHostState.showSnackbar("未识别到文字，请重试")
                                }
                            }.onFailure { e ->
                                snackbarHostState.showSnackbar("识别失败: ${e.message}")
                            }
                            voiceRecognizer.clearBuffer()
                        }
                    },
                    onSwitchToKeyboard = { isVoiceMode = false },
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                TextInputBar(
                    inputText = uiState.inputText,
                    onInputChange = viewModel::onInputChange,
                    onSend = viewModel::sendMessage,
                    onSwitchToVoice = { isVoiceMode = true },
                    onCameraClick = {},
                    isStreaming = uiState.isStreaming,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun VoiceInputBar(
    isRecording: Boolean,
    showHint: Boolean,
    onShowHintChange: (Boolean) -> Unit,
    onPressStart: () -> Unit,
    onPressEnd: (Long) -> Unit,
    onSwitchToKeyboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var pressStartTime by remember { mutableStateOf(0L) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "color"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onSwitchToKeyboard) {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = "键盘模式",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .scale(buttonScale)
                    .shadow(if (isRecording) 12.dp else 4.dp, CircleShape)
                    .background(buttonColor, CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                pressStartTime = System.currentTimeMillis()
                                onShowHintChange(false)

                                val hintJob = scope.launch {
                                    delay(1000)
                                    onShowHintChange(true)
                                }

                                onPressStart()
                                tryAwaitRelease()
                                hintJob.cancel()

                                val duration = System.currentTimeMillis() - pressStartTime
                                onPressEnd(duration)
                            }
                        )
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = when {
                            isRecording -> "录音中"
                            showHint -> "请长按说话"
                            else -> "按住"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Box(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun TextInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSwitchToVoice: () -> Unit,
    onCameraClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                placeholder = { Text("输入消息...") },
                maxLines = 4,
                enabled = !isStreaming,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
            )

            FloatingActionButton(
                onClick = onCameraClick,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "拍照",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            FloatingActionButton(
                onClick = onSwitchToVoice,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "语音输入",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(44.dp),
                containerColor = if (inputText.isNotBlank() && !isStreaming)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (inputText.isNotBlank() && !isStreaming)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}