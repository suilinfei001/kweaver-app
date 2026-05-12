package com.kweaver.dip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kweaver.dip.data.repository.AiConfigRepository
import com.kweaver.dip.ui.navigation.AppNavigation
import com.kweaver.dip.ui.theme.KWeaverDIPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var aiConfigRepository: AiConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KWeaverDIPTheme {
                val hasConfig by aiConfigRepository.hasConfig.collectAsState(initial = false)
                AppNavigation(hasConfig = hasConfig)
            }
        }
    }
}