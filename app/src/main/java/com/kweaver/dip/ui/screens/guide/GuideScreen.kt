package com.kweaver.dip.ui.screens.guide

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Guide") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress
            LinearProgressIndicator(
                progress = { (uiState.step + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            when (uiState.step) {
                0 -> {
                    Text("Checking status...", style = MaterialTheme.typography.titleLarge)
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    }
                }
                1 -> {
                    Text("OpenClaw Configuration", style = MaterialTheme.typography.titleLarge)
                    Text("Connect to the OpenClaw gateway service.", style = MaterialTheme.typography.bodyMedium)

                    OutlinedTextField(
                        value = uiState.openclawAddress,
                        onValueChange = viewModel::updateOpenclawAddress,
                        label = { Text("OpenClaw Address *") },
                        placeholder = { Text("http://openclaw:8080") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.openclawToken,
                        onValueChange = viewModel::updateOpenclawToken,
                        label = { Text("OpenClaw Token *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::prevStep, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = viewModel::nextStep,
                            enabled = uiState.openclawAddress.isNotBlank() && uiState.openclawToken.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) { Text("Next") }
                    }
                }
                2 -> {
                    Text("KWeaver Integration (Optional)", style = MaterialTheme.typography.titleLarge)
                    Text("Optionally connect to KWeaver Core.", style = MaterialTheme.typography.bodyMedium)

                    OutlinedTextField(
                        value = uiState.kweaverBaseUrl,
                        onValueChange = viewModel::updateKweaverBaseUrl,
                        label = { Text("KWeaver Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.kweaverToken,
                        onValueChange = viewModel::updateKweaverToken,
                        label = { Text("KWeaver Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::prevStep, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = viewModel::initialize,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Initialize")
                        }
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            Modifier.size(80.dp),
                            MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Setup Complete!", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("KWeaver DIP is ready to use.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) { Text("Done") }
                    }
                }
            }
        }
    }
}
