package com.example

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.UniCafeDailyTheme
import com.example.ui.viewmodel.UniCafeViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: UniCafeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val locale = remember(uiState.appLanguage) {
                when (uiState.appLanguage) {
                    "fi" -> Locale("fi", "FI")
                    "sv" -> Locale("sv", "SE")
                    else -> Locale.ENGLISH
                }
            }
            val context = LocalContext.current
            val localizedContext = remember(context, locale) {
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                val locList = LocaleList(locale)
                LocaleList.setDefault(locList)
                config.setLocales(locList)
                context.createConfigurationContext(config)
            }
            val localizedConfiguration = remember(context, locale) {
                Configuration(context.resources.configuration).apply {
                    setLocale(locale)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                UniCafeDailyTheme {
                    MainAppScaffold(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

