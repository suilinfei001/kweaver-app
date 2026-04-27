package com.kweaver.dip.ui.screens.skills

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kweaver.dip.data.model.Skill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SkillListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            val fileName = it.lastPathSegment ?: "skill.zip"
            if (bytes != null) viewModel.installSkill(fileName, bytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skills") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePicker.launch("application/zip") }) {
                Icon(Icons.Default.Upload, "Upload Skill")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.skills.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Extension, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No skills installed", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            else -> {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    uiState.uploadStatus?.let { status ->
                        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(status, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    LazyColumn {
                        items(uiState.skills, key = { it.name }) { skill ->
                            ListItem(
                                headlineContent = { Text(skill.name) },
                                supportingContent = { skill.description?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) } },
                                leadingContent = {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Extension, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                },
                                modifier = Modifier.clickable { onNavigateToDetail(skill.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}
