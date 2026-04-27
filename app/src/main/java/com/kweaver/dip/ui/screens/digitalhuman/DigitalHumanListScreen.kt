package com.kweaver.dip.ui.screens.digitalhuman

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
import com.kweaver.dip.data.model.DigitalHuman

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalHumanListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSkills: () -> Unit,
    viewModel: DigitalHumanListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<DigitalHuman?>(null) }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Digital Human") },
            text = { Text("Are you sure you want to delete \"${deleteTarget!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDigitalHuman(deleteTarget!!.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Humans") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Manage Skills") },
                                onClick = { showMenu = false; onNavigateToSkills() },
                                leadingIcon = { Icon(Icons.Default.Extension, null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Create Digital Human")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.digitalHumans.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SmartToy, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No digital humans", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onNavigateToCreate) { Text("Create One") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.digitalHumans, key = { it.id }) { dh ->
                        DigitalHumanListItem(
                            digitalHuman = dh,
                            onClick = { onNavigateToDetail(dh.id) },
                            onDelete = { deleteTarget = dh }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalHumanListItem(
    digitalHuman: DigitalHuman,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(digitalHuman.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            digitalHuman.creature?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
