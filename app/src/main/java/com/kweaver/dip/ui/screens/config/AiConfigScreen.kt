package com.kweaver.dip.ui.screens.config

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "AI 模型配置",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "配置你的 AI 大模型端点信息",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.baseUrl,
            onValueChange = viewModel::onBaseUrlChange,
            label = { Text("Base URL") },
            placeholder = { Text("https://api.openai.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.modelId,
            onValueChange = viewModel::onModelIdChange,
            label = { Text("Model ID") },
            placeholder = { Text("gpt-4o") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.apiKey,
            onValueChange = viewModel::onApiKeyChange,
            label = { Text("API Key") },
            singleLine = true,
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(
                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showApiKey) "隐藏" else "显示",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.contextSize,
            onValueChange = viewModel::onContextSizeChange,
            label = { Text("Context Size") },
            placeholder = { Text("4096") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = viewModel::testConnection,
                enabled = !uiState.isTesting,
                modifier = Modifier.weight(1f),
            ) {
                if (uiState.isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("测试连接")
            }

            Button(
                onClick = viewModel::saveConfig,
                enabled = uiState.testSuccess == true && !uiState.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text("保存")
            }
        }

        uiState.testSuccess?.let { success ->
            if (success) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✓ 连接成功", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        uiState.testError?.let { error ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("✗ $error", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
