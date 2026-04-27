package com.kweaver.dip.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToGuide: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(uiState.serverUrl) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Sign Out") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User info
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Account", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    uiState.username?.let {
                        ListItem(
                            headlineContent = { Text("Username") },
                            supportingContent = { Text(it) },
                            leadingContent = { Icon(Icons.Default.Person, null) }
                        )
                    }
                    uiState.userId?.let {
                        ListItem(
                            headlineContent = { Text("User ID") },
                            supportingContent = { Text(it) },
                            leadingContent = { Icon(Icons.Default.Badge, null) }
                        )
                    }
                }
            }

            // Server config
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Server", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.updateServerUrl(serverUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Server URL") }
                }
            }

            // Actions
            Card(Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Setup Guide") },
                        supportingContent = { Text("Configure OpenClaw integration") },
                        leadingContent = { Icon(Icons.Default.SettingsSuggest, null) },
                        modifier = Modifier.clickable { onNavigateToGuide() }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Sign Out") },
                        leadingContent = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { showLogoutDialog = true }
                    )
                }
            }

            // App info
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    ListItem(
                        headlineContent = { Text("KWeaver DIP") },
                        supportingContent = { Text("Version 1.0.0") },
                        leadingContent = { Icon(Icons.Default.Info, null) }
                    )
                }
            }
        }
    }
}
