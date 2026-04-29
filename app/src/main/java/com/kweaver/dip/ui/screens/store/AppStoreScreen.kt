package com.kweaver.dip.ui.screens.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kweaver.dip.data.model.ApplicationInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppStoreScreen(
    viewModel: AppStoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Store") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.apps.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::loadApps) { Text("Retry") }
                    }
                }
            }
            uiState.apps.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No apps installed", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Apps you install will appear here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    val pinnedApps = uiState.apps.filter { it.pinned }
                    val otherApps = uiState.apps.filter { !it.pinned }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (pinnedApps.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    "Pinned",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(pinnedApps, key = { "pinned-${it.key}" }) { app ->
                                AppCard(
                                    app = app,
                                    onPinToggle = { viewModel.togglePin(app.key, !app.pinned) },
                                    onUninstall = { viewModel.uninstall(app.key) }
                                )
                            }
                        }
                        if (otherApps.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    "All Apps",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = if (pinnedApps.isNotEmpty()) 8.dp else 0.dp, bottom = 4.dp)
                                )
                            }
                            items(otherApps, key = { it.key }) { app ->
                                AppCard(
                                    app = app,
                                    onPinToggle = { viewModel.togglePin(app.key, !app.pinned) },
                                    onUninstall = { viewModel.uninstall(app.key) }
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
private fun AppCard(
    app: ApplicationInfo,
    onPinToggle: () -> Unit,
    onUninstall: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (app.pinned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = if (app.pinned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            app.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onPinToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (app.pinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (app.pinned) "Unpin" else "Pin",
                        modifier = Modifier.size(18.dp),
                        tint = if (app.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onUninstall, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Uninstall",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
