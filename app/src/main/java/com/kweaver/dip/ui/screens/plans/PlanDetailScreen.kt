package com.kweaver.dip.ui.screens.plans

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
import com.kweaver.dip.data.model.CronRunEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(planId) { viewModel.loadPlan(planId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plan?.name ?: "Plan") },
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
                uiState.plan?.let { plan ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(plan.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Row {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(if (plan.enabled) "Enabled" else "Disabled") },
                                    leadingIcon = {
                                        Icon(
                                            if (plan.enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            null, Modifier.size(18.dp)
                                        )
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                plan.schedule.expr?.let {
                                    AssistChip(onClick = {}, label = { Text(it) }, leadingIcon = { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) })
                                }
                            }
                        }
                    }
                }

                if (uiState.content.isNotBlank()) {
                    Text("Plan Content", style = MaterialTheme.typography.titleMedium)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            uiState.content,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (uiState.runs.isNotEmpty()) {
                    Text("Run History", style = MaterialTheme.typography.titleMedium)
                    uiState.runs.forEach { run ->
                        RunHistoryItem(run)
                    }
                }
            }
        }
    }
}

@Composable
private fun RunHistoryItem(run: CronRunEntry) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (run.status) {
                    "completed" -> Icons.Default.CheckCircle
                    "failed" -> Icons.Default.Error
                    else -> Icons.Default.Pending
                },
                null,
                tint = when (run.status) {
                    "completed" -> MaterialTheme.colorScheme.primary
                    "failed" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(formatDate(run.ts), style = MaterialTheme.typography.bodySmall)
                run.summary?.let { Text(it, maxLines = 2, style = MaterialTheme.typography.bodySmall) }
                run.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
            run.durationMs?.let { dur ->
                Text("${dur / 1000}s", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
