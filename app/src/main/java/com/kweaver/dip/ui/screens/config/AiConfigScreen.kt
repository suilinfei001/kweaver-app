package com.kweaver.dip.ui.screens.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AiConfigScreen(
    onConfigSaved: () -> Unit,
    viewModel: AiConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showApiKey by rememberSaveable { mutableStateOf(false) }

    if (uiState.saved) {
        onConfigSaved()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "配置你的 AI 助手",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        AiModelConfigCard(
            baseUrl = uiState.baseUrl,
            modelId = uiState.modelId,
            apiKey = uiState.apiKey,
            contextSize = uiState.contextSize,
            showApiKey = showApiKey,
            testStatus = uiState.aiTestStatus,
            testError = uiState.aiTestError,
            onBaseUrlChange = viewModel::onBaseUrlChange,
            onModelIdChange = viewModel::onModelIdChange,
            onApiKeyChange = viewModel::onApiKeyChange,
            onContextSizeChange = viewModel::onContextSizeChange,
            onToggleApiKeyVisibility = { showApiKey = !showApiKey },
            onTestClick = viewModel::testAiConnection,
        )

        AsrConfigCard(
            asrEnabled = uiState.asrEnabled,
            asrUrl = uiState.asrUrl,
            testStatus = uiState.asrTestStatus,
            testError = uiState.asrTestError,
            onAsrEnabledChange = viewModel::onAsrEnabledChange,
            onAsrUrlChange = viewModel::onAsrUrlChange,
            onTestClick = viewModel::testAsrConnection,
        )

        TtsConfigCard(
            ttsEnabled = uiState.ttsEnabled,
            ttsUrl = uiState.ttsUrl,
            testStatus = uiState.ttsTestStatus,
            testError = uiState.ttsTestError,
            onTtsEnabledChange = viewModel::onTtsEnabledChange,
            onTtsUrlChange = viewModel::onTtsUrlChange,
            onTestClick = viewModel::testTtsConnection,
        )

        Button(
            onClick = viewModel::saveConfig,
            enabled = uiState.canSave && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("保存配置")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AiModelConfigCard(
    baseUrl: String,
    modelId: String,
    apiKey: String,
    contextSize: String,
    showApiKey: Boolean,
    testStatus: TestStatus,
    testError: String?,
    onBaseUrlChange: (String) -> Unit,
    onModelIdChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onContextSizeChange: (String) -> Unit,
    onToggleApiKeyVisibility: () -> Unit,
    onTestClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI 模型配置",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                TestStatusIndicator(status = testStatus, error = testError)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                label = { Text("Base URL") },
                placeholder = { Text("https://api.openai.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = modelId,
                onValueChange = onModelIdChange,
                label = { Text("Model ID") },
                placeholder = { Text("gpt-4o") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleApiKeyVisibility) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "隐藏" else "显示",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contextSize,
                onValueChange = onContextSizeChange,
                label = { Text("Context Size") },
                placeholder = { Text("4096") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onTestClick,
                enabled = testStatus != TestStatus.TESTING,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (testStatus == TestStatus.TESTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("测试 AI 连接")
            }

            AnimatedVisibility(visible = testError != null && testStatus == TestStatus.FAILED) {
                Text(
                    text = testError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AsrConfigCard(
    asrEnabled: Boolean,
    asrUrl: String,
    testStatus: TestStatus,
    testError: String?,
    onAsrEnabledChange: (Boolean) -> Unit,
    onAsrUrlChange: (String) -> Unit,
    onTestClick: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "语音识别 (ASR)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TestStatusIndicator(status = testStatus, error = testError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = asrEnabled,
                        onCheckedChange = onAsrEnabledChange,
                    )
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "收起" else "展开",
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = asrUrl,
                        onValueChange = onAsrUrlChange,
                        label = { Text("ASR 服务地址") },
                        placeholder = { Text("http://xxx:8348/asr") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = asrEnabled,
                    )

                    AnimatedVisibility(visible = asrEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onTestClick,
                                enabled = testStatus != TestStatus.TESTING,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (testStatus == TestStatus.TESTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("测试 ASR 连接")
                            }

                            AnimatedVisibility(visible = testError != null && testStatus == TestStatus.FAILED) {
                                Text(
                                    text = testError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TtsConfigCard(
    ttsEnabled: Boolean,
    ttsUrl: String,
    testStatus: TestStatus,
    testError: String?,
    onTtsEnabledChange: (Boolean) -> Unit,
    onTtsUrlChange: (String) -> Unit,
    onTestClick: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SpeakerPhone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "语音合成 (TTS)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TestStatusIndicator(status = testStatus, error = testError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = ttsEnabled,
                        onCheckedChange = onTtsEnabledChange,
                    )
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "收起" else "展开",
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = ttsUrl,
                        onValueChange = onTtsUrlChange,
                        label = { Text("TTS 服务地址") },
                        placeholder = { Text("http://xxx:8349") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ttsEnabled,
                    )

                    AnimatedVisibility(visible = ttsEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onTestClick,
                                enabled = testStatus != TestStatus.TESTING,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (testStatus == TestStatus.TESTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("测试 TTS 连接")
                            }

                            AnimatedVisibility(visible = testError != null && testStatus == TestStatus.FAILED) {
                                Text(
                                    text = testError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestStatusIndicator(
    status: TestStatus,
    error: String?,
) {
    when (status) {
        TestStatus.NONE -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "未测试",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )
        }
        TestStatus.TESTING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        }
        TestStatus.SUCCESS -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "测试通过",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        TestStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "测试失败: ${error ?: ""}",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}