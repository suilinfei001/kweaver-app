package com.kweaver.dip.ui.screens.chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

enum class ChatScreenVariant {
    ORIGINAL, VA, VB, VC
}

@Composable
fun ChatScreenPreview(
    onNavigateToSettings: () -> Unit,
    initialVariant: ChatScreenVariant = ChatScreenVariant.ORIGINAL,
) {
    var currentVariant by remember { mutableStateOf(initialVariant) }

    when (currentVariant) {
        ChatScreenVariant.ORIGINAL -> {
            ChatScreenPreviewSelector(
                onSelectVA = { currentVariant = ChatScreenVariant.VA },
                onSelectVB = { currentVariant = ChatScreenVariant.VB },
                onSelectVC = { currentVariant = ChatScreenVariant.VC },
                modifier = Modifier.fillMaxSize(),
            )
        }
        ChatScreenVariant.VA -> {
            ChatScreenVA(
                onNavigateToSettings = onNavigateToSettings,
            )
        }
        ChatScreenVariant.VB -> {
            ChatScreenVB(
                onNavigateToSettings = onNavigateToSettings,
            )
        }
        ChatScreenVariant.VC -> {
            ChatScreenVC(
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}