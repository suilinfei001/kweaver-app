package com.kweaver.dip.ui.screens.skills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(
    skillName: String,
    onNavigateBack: () -> Unit,
    viewModel: SkillDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Files")

    LaunchedEffect(skillName) { viewModel.loadSkill(skillName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(skillName) },
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
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }

                when (selectedTab) {
                    0 -> InfoTab(uiState.content)
                    1 -> FilesTab(uiState.tree)
                }
            }
        }
    }
}

@Composable
private fun InfoTab(content: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (content.isBlank()) {
            Text("No content available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FilesTab(tree: List<com.kweaver.dip.data.model.SkillTreeItem>) {
    if (tree.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No files", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            tree.forEach { item -> SkillTreeItemRow(item, depth = 0) }
        }
    }
}

@Composable
private fun SkillTreeItemRow(item: com.kweaver.dip.data.model.SkillTreeItem, depth: Int) {
    Row(modifier = Modifier.padding(start = (depth * 24).dp, vertical = 4.dp)) {
        Text(
            if (item.type == "directory") "📁 " else "📄 ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(item.name, style = MaterialTheme.typography.bodyMedium)
    }
    item.children?.forEach { child -> SkillTreeItemRow(child, depth + 1) }
}
