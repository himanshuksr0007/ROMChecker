package com.romchecker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.romchecker.app.ui.*
import com.romchecker.app.ui.components.*
import com.romchecker.app.ui.screens.*
import com.romchecker.app.ui.theme.ROMCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ROMCheckerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ROMCheckerApp()
                }
            }
        }
    }
}

@Composable
fun ROMCheckerApp() {
    val viewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(androidx.compose.ui.platform.LocalContext.current))
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(bottomBar = {
        NavigationBar {
            listOf(
                Triple(0, Icons.Default.Home, "Home"),
                Triple(1, Icons.Default.Security, "Security"),
                Triple(2, Icons.Default.Description, "ROM"),
                Triple(3, Icons.Default.Key, "Keys"),
                Triple(4, Icons.Default.Settings, "Settings")
            ).forEach { (idx, icon, label) ->
                NavigationBarItem(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    icon = { Icon(icon, label) },
                    label = { Text(label) }
                )
            }
        }
    }) { padding ->
        Box(Modifier.padding(padding)) {
            when (val state = uiState) {
                is SecurityUiState.Loading -> LoadingScreen()
                is SecurityUiState.Error -> ErrorScreen(message = state.message,
                    onRetry = { viewModel.performSecurityScan() }
                )
                is SecurityUiState.Success -> when (selectedTab) {
                    // To this:
                    0 -> DashboardScreen(
                        securityStatus = state.securityStatus,
                        onScanClick = { viewModel.performSecurityScan() }
                    )
                    1 -> DeviceSecurityScreen(state.securityStatus.playIntegrity, state.securityStatus.rootStatus, state.securityStatus.romSignature)
                    2 -> ROMAnalysisScreen(state.securityStatus.romSignature)
                    3 -> KeyAttestationScreen(state.securityStatus.keyAttestation)
                    4 -> SettingsScreen(state.securityStatus)
                }
            }
        }
    }
}
