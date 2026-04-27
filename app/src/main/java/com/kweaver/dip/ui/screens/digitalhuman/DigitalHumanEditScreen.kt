package com.kweaver.dip.ui.screens.digitalhuman

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DigitalHumanEditScreen(
    digitalHumanId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DigitalHumanEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(digitalHumanId) { viewModel.init(digitalHumanId) }
    LaunchedEffect(uiState.saveSuccess) { if (uiState.saveSuccess) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEdit) "Edit Digital Human" else "Create Digital Human") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.creature,
                    onValueChange = viewModel::updateCreature,
                    label = { Text("Role / Creature") },
                    placeholder = { Text("e.g. Data Analyst") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.soul,
                    onValueChange = viewModel::updateSoul,
                    label = { Text("Soul / Description") },
                    placeholder = { Text("Describe the digital human's personality and purpose...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                // BKN entries
                Text("Knowledge Networks", style = MaterialTheme.typography.titleMedium)
                uiState.bknEntries.forEachIndexed { index, entry ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = entry.name,
                            onValueChange = { viewModel.updateBknEntry(index, it, entry.url) },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = entry.url,
                            onValueChange = { viewModel.updateBknEntry(index, entry.name, it) },
                            label = { Text("URL") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeBknEntry(index) }) {
                            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                OutlinedButton(onClick = viewModel::addBknEntry, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Knowledge Network")
                }

                // Skills selection
                Text("Skills", style = MaterialTheme.typography.titleMedium)
                if (uiState.availableSkills.isEmpty()) {
                    Text("No skills available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.availableSkills.forEach { skill ->
                            FilterChip(
                                selected = skill.name in uiState.selectedSkills,
                                onClick = { viewModel.toggleSkill(skill.name) },
                                label = { Text(skill.name) }
                            )
                        }
                    }
                }

                // Channel config
                var channelExpanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { channelExpanded = !channelExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (channelExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Channel Configuration")
                }

                if (channelExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        var channelType by remember { mutableStateOf(uiState.channelType) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = channelType == "feishu", onClick = { channelType = "feishu"; viewModel.updateChannelType("feishu") }, label = { Text("Feishu") })
                            FilterChip(selected = channelType == "dingtalk", onClick = { channelType = "dingtalk"; viewModel.updateChannelType("dingtalk") }, label = { Text("DingTalk") })
                        }
                        OutlinedTextField(value = uiState.channelAppId, onValueChange = viewModel::updateChannelAppId, label = { Text("App ID") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = uiState.channelAppSecret, onValueChange = viewModel::updateChannelAppSecret, label = { Text("App Secret") }, modifier = Modifier.fillMaxWidth())
                    }
                }

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { viewModel.save(digitalHumanId) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (uiState.isEdit) "Save Changes" else "Create")
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
