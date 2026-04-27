package com.kweaver.dip.ui.screens.digitalhuman

import androidx.compose.foundation.layout.*
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
import com.kweaver.dip.data.model.DigitalHumanDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalHumanDetailScreen(
    digitalHumanId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToChat: (agentId: String, agentName: String) -> Unit,
    viewModel: DigitalHumanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profile", "Skills", "Channel")

    LaunchedEffect(digitalHumanId) { viewModel.loadDetail(digitalHumanId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.detail?.name ?: "Digital Human") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    uiState.detail?.let { dh ->
                        IconButton(onClick = { onNavigateToEdit(dh.id) }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            uiState.detail?.let { dh ->
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToChat(dh.id, dh.name) },
                    icon = { Icon(Icons.Default.Chat, null) },
                    text = { Text("Start Chat") }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            uiState.detail?.let { detail ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                        }
                    }

                    when (selectedTab) {
                        0 -> ProfileTab(detail)
                        1 -> SkillsTab(detail)
                        2 -> ChannelTab(detail)
                    }
                }
            }
        }
        }
    }

@Composable
private fun ProfileTab(detail: DigitalHumanDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(detail.name, style = MaterialTheme.typography.titleLarge)
                detail.creature?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }

        Spacer(Modifier.height(24.dp))

        detail.soul?.let { soul ->
            Text("Soul / Description", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    soul,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        detail.bkn?.takeIf { it.isNotEmpty() }?.let { bknList ->
            Spacer(Modifier.height(24.dp))
            Text("Knowledge Networks", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            bknList.forEach { bkn ->
                ListItem(
                    headlineContent = { Text(bkn.name) },
                    supportingContent = { Text(bkn.url, maxLines = 1) }
                )
            }
        }
    }
}

@Composable
private fun SkillsTab(detail: DigitalHumanDetail) {
    if (detail.skills.isNullOrEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No skills configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            detail.skills.forEach { skill ->
                AssistChip(
                    onClick = {},
                    label = { Text(skill) },
                    leadingIcon = { Icon(Icons.Default.Extension, null, Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun ChannelTab(detail: DigitalHumanDetail) {
    detail.channel?.let { channel ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            channel.type?.let { type ->
                ListItem(headlineContent = { Text("Type") }, supportingContent = { Text(type) })
            }
            channel.appId?.let { appId ->
                ListItem(headlineContent = { Text("App ID") }, supportingContent = { Text(appId) })
            }
            channel.appSecret?.let { secret ->
                ListItem(headlineContent = { Text("App Secret") }, supportingContent = { Text("••••••••") })
            }
        }
    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No channel configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
