package com.romchecker.app.ui.screens
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
import com.romchecker.app.domain.model.*

@Composable
fun KeyAttestationScreen(keyAttestation: KeyAttestationResult?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Key Attestation", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))
        if (keyAttestation == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Use the named parameter
                ) {
                    Icon(Icons.Default.Security, null, Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Unavailable", style = MaterialTheme.typography.titleMedium)
                }

            }
        } else {
            Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Security Level", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(keyAttestation.securityLevel.name.replace("_", " "), 
                        style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Hardware-Backed")
                        Text(if (keyAttestation.isHardwareBacked) "Yes" else "No", 
                            color = if (keyAttestation.isHardwareBacked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Boot State")
                        Text(keyAttestation.bootState)
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Bootloader")
                        Text(if (keyAttestation.bootloaderLocked) "Locked" else "Unlocked",
                            color = if (keyAttestation.bootloaderLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                }
            }

            var expanded by remember { mutableStateOf(false) }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Certificates (${keyAttestation.certificates.size})")
                        IconButton({ expanded = !expanded }) {
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                    }
                    if (expanded) {
                        keyAttestation.certificates.forEachIndexed { i, cert ->
                            Spacer(Modifier.height(8.dp))
                            Text("Cert #${i+1}", style = MaterialTheme.typography.labelMedium)
                            Text(cert.take(100) + "...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
