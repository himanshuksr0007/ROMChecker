package com.romchecker.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.romchecker.app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ROMAnalysisScreen(romSignature: ROMSignatureResult, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    Column(modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        Text("ROM Signature",  style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        Card(Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Type", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                // FIX 1: Combine color into the style using .copy()
                Text(romSignature.signatureType.name.replace("_", " "),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        // FIX 2: Add the 'modifier =' named parameter
        Text("Certificates (${romSignature.certificates.size})",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        romSignature.certificates.forEachIndexed { i, cert ->
            Card(Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Certificate #${i+1}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Issuer: ${cert.issuer.take(60)}...", style = MaterialTheme.typography.bodySmall)
                    Text("Subject: ${cert.subject.take(60)}...", style = MaterialTheme.typography.bodySmall)
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("SHA-256", style = MaterialTheme.typography.labelLarge)
                        IconButton({ clipboard.setText(AnnotatedString(cert.sha256Fingerprint)) }, Modifier.size(24.dp)) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                        }
                    }
                    Text(
                        text = cert.sha256Fingerprint,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                }
            }
        }
    }
}
