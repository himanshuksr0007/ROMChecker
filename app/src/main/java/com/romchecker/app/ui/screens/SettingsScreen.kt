package com.romchecker.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import com.romchecker.app.domain.model.SecurityStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(securityStatus: SecurityStatus?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJson by remember { mutableStateOf("") }

    Column(modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        // FIX: Reordered modifier and style
        Text("Settings", modifier = Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                // FIX: Added 'modifier =' named parameter
                Text("Appearance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme")
                    // FIX: Combined color into style using .copy()
                    Text(
                        "System Default",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        if (securityStatus != null) {
            Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    // FIX: Added 'modifier =' named parameter
                    Text("Data", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
                    Button(
                        onClick = {
                            exportJson = generateExportJson(securityStatus)
                            showExportDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Scan Results")
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            // FIX: Used 'horizontalAlignment' named parameter
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // FIX: Used named arguments for modifier and tint
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text("ROMChecker", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                // FIX: Combined color into style using .copy()
                Text(
                    "Version 1.0.4",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            json = exportJson,
            onDismiss = { showExportDialog = false },
            onSave = { saveAndShareJson(context, exportJson); showExportDialog = false }
        )
    }
}

@Composable
private fun ExportDialog(json: String, onDismiss: () -> Unit, onSave: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Results") },
        text = {
            Column {
                Text("Scan results:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // FIX: Correctly implemented a scrollable text area
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = json,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save & Share") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun generateExportJson(status: SecurityStatus): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val data = mapOf(
        "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(status.timestamp)),
        "playIntegrity" to mapOf(
            "verdict" to status.playIntegrity.verdict.name,
            "deviceIntegrity" to status.playIntegrity.deviceIntegrity,
            "basicIntegrity" to status.playIntegrity.basicIntegrity,
            "strongIntegrity" to status.playIntegrity.strongIntegrity,
            "error" to status.playIntegrity.errorMessage
        ),
        "rootStatus" to mapOf(
            "isRooted" to status.rootStatus.isRooted,
            "rootType" to status.rootStatus.rootType.name,
            "isCustomROM" to status.rootStatus.isCustomROM,
            "detections" to status.rootStatus.detectionMethods.map {
                mapOf("name" to it.name, "detected" to it.detected, "details" to it.details)
            }
        ),
        "bootloader" to mapOf(
            "state" to status.bootloaderStatus.state.name,
            "verifiedBootState" to status.bootloaderStatus.verifiedBootState.name,
            "details" to status.bootloaderStatus.details
        ),
        "romSignature" to mapOf(
            "type" to status.romSignature.signatureType.name,
            "certificateCount" to status.romSignature.certificates.size,
            "isValid" to status.romSignature.isValid
        ),
        "keyAttestation" to if (status.keyAttestation != null) mapOf(
            "hardwareBacked" to status.keyAttestation.isHardwareBacked,
            "bootState" to status.keyAttestation.bootState,
            "bootloaderLocked" to status.keyAttestation.bootloaderLocked,
            "securityLevel" to status.keyAttestation.securityLevel.name
        ) else null
    )
    return gson.toJson(data)
}

private fun saveAndShareJson(context: Context, json: String) {
    try {
        val fileName = "romchecker_scan_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(json)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_TITLE, "ROMChecker Scan Results")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share scan results"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
