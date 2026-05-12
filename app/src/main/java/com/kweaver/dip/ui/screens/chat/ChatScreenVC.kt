package com.kweaver.dip.ui.screens.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ChatScreenVC(
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

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
    )

    val recordingGradientColors = listOf(
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.errorContainer,
    )

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
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
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
                VoiceInputBarVC(
                    isRecording = isRecording,
                    showHint = showHint,
                    onShowHintChange = { showHint = it },
                    onPressStart = {
                        if (!hasRecordPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@VoiceInputBarVC
                        }
                        if (!uiState.config.asrEnabled || uiState.config.asrUrl.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请先在设置中配置 ASR 服务")
                            }
                            return@VoiceInputBarVC
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
                            return@VoiceInputBarVC
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
                TextInputBarVC(
                    inputText = uiState.inputText,
                    onInputChange = viewModel::onInputChange,
                    onSend = viewModel::sendMessage,
                    onSwitchToVoice = { isVoiceMode = true },
                    isStreaming = uiState.isStreaming,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun VoiceInputBarVC(
    isRecording: Boolean,
    showHint: Boolean,
    onShowHintChange: (Boolean) -> Unit,
    onPressStart: () -> Unit,
    onPressEnd: (Long) -> Unit,
    onSwitchToKeyboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressStartTime by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "voiceWave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveScale"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveAlpha"
    )

    val gradientColors = if (isRecording) {
        listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
    } else if (showHint) {
        listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onSwitchToKeyboard,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = "键盘",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp),
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(waveScale)
                                .background(
                                    gradientColors[0].copy(alpha = waveAlpha),
                                    CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.verticalGradient(gradientColors),
                                shape = CircleShape,
                            )
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
                            },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                text = when {
                                    isRecording -> "录音"
                                    showHint -> "请说话"
                                    else -> "按住"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    isRecording -> "松开后自动识别"
                    showHint -> "还在说话吗？"
                    else -> "长按开始说话"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TextInputBarVC(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSwitchToVoice: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp,
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
                shape = RoundedCornerShape(20.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onSwitchToVoice,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "语音",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onSend,
                shape = CircleShape,
                color = if (inputText.isNotBlank() && !isStreaming)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (inputText.isNotBlank() && !isStreaming)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}