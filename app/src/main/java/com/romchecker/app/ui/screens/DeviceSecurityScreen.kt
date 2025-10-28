package com.romchecker.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.os.Build
import com.romchecker.app.domain.model.*
import com.romchecker.app.ui.components.DetectionItem

@Composable
fun DeviceSecurityScreen(playIntegrity: PlayIntegrityResult, rootStatus: RootDetectionResult, 
    romSignature: ROMSignatureResult, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Play Integrity", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))
        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Strong")
                    Text(if (playIntegrity.strongIntegrity) "✓" else "✗", 
                        color = if (playIntegrity.strongIntegrity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Device")
                    Text(if (playIntegrity.deviceIntegrity) "✓" else "✗",
                        color = if (playIntegrity.deviceIntegrity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Basic")
                    Text(if (playIntegrity.basicIntegrity) "✓" else "✗",
                        color = if (playIntegrity.basicIntegrity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                playIntegrity.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Text("ROM Signature", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))
        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Signature: ${romSignature.signatureType.name.replace("_", " ")}", style = MaterialTheme.typography.bodyLarge)
                Text("Build tags: ${Build.TAGS ?: "unknown"}", style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("Root Detection", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))
        Text("Status: ${if (rootStatus.isRooted) "Rooted" else "Not Rooted"}", 
            style = MaterialTheme.typography.bodyLarge, 
            color = if (rootStatus.isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp))
        rootStatus.detectionMethods.forEach { DetectionItem(it.name, it.detected, it.details) }
    }
}
