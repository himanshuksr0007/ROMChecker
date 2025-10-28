package com.romchecker.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.romchecker.app.domain.model.*
import com.romchecker.app.ui.components.StatusCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(securityStatus: SecurityStatus, onScanClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("ROMchecker", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))

        StatusCard("Play Integrity", getIntegrityText(securityStatus.playIntegrity.verdict), 
            getIntegrityColor(securityStatus.playIntegrity.verdict),
            { Icon(Icons.Default.Security, null, Modifier.size(40.dp), getIntegrityColor(securityStatus.playIntegrity.verdict)) },
            securityStatus.playIntegrity.errorMessage ?: "")

        StatusCard("Root Status", if (securityStatus.rootStatus.isRooted) "Rooted" else "Not Rooted",
            if (securityStatus.rootStatus.isRooted) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
            { Icon(if (securityStatus.rootStatus.isRooted) Icons.Default.Warning else Icons.Default.CheckCircle, 
                null, Modifier.size(40.dp), 
                if (securityStatus.rootStatus.isRooted) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)) },
            when (securityStatus.rootStatus.rootType) {
                RootType.MAGISK -> "Magisk"
                RootType.KERNELSU -> "KernelSU"
                RootType.KITSUNE -> "Kitsune"
                RootType.SUPERSU -> "SuperSU"
                RootType.UNKNOWN -> "Unknown Root"
                RootType.NONE -> if (securityStatus.rootStatus.isCustomROM) "Custom ROM" else ""
            })

        StatusCard("Bootloader", securityStatus.bootloaderStatus.state.name, getBootloaderColor(securityStatus.bootloaderStatus.state),
            { Icon(when(securityStatus.bootloaderStatus.state) { 
                BootloaderState.LOCKED -> Icons.Default.Lock
                BootloaderState.UNLOCKED -> Icons.Default.LockOpen
                else -> Icons.Default.Help }, null, Modifier.size(40.dp), 
                getBootloaderColor(securityStatus.bootloaderStatus.state)) },
            "VB: ${securityStatus.bootloaderStatus.verifiedBootState.name}")

        StatusCard("ROM Signature", securityStatus.romSignature.signatureType.name.replace("_", " "), 
            getSignatureColor(securityStatus.romSignature.signatureType),
            { Icon(Icons.Default.VerifiedUser, null, Modifier.size(40.dp), 
                getSignatureColor(securityStatus.romSignature.signatureType)) },
            "${securityStatus.romSignature.certificates.size} certs")

        Spacer(Modifier.height(16.dp))
        Button(onScanClick, Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.Refresh, null, Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Re-scan")
        }

        Text("Last Fetched: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(securityStatus.timestamp))}",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp))
    }
}

private fun getIntegrityText(v: IntegrityVerdict) = when(v) {
    IntegrityVerdict.MEETS_STRONG_INTEGRITY -> "Strong"
    IntegrityVerdict.MEETS_DEVICE_INTEGRITY -> "Device"
    IntegrityVerdict.MEETS_BASIC_INTEGRITY -> "Basic"
    IntegrityVerdict.FAILED -> "Failed"
    IntegrityVerdict.UNKNOWN -> "Unknown"
}

private fun getIntegrityColor(v: IntegrityVerdict) = when(v) {
    IntegrityVerdict.MEETS_STRONG_INTEGRITY -> Color(0xFF4CAF50)
    IntegrityVerdict.MEETS_DEVICE_INTEGRITY -> Color(0xFF8BC34A)
    IntegrityVerdict.MEETS_BASIC_INTEGRITY -> Color(0xFFFFC107)
    IntegrityVerdict.FAILED -> Color(0xFFF44336)
    IntegrityVerdict.UNKNOWN -> Color(0xFF9E9E9E)
}

private fun getBootloaderColor(s: BootloaderState) = when(s) {
    BootloaderState.LOCKED -> Color(0xFF4CAF50)
    BootloaderState.UNLOCKED -> Color(0xFFF44336)
    BootloaderState.UNKNOWN -> Color(0xFF9E9E9E)
}

private fun getSignatureColor(t: SignatureType) = when(t) {
    SignatureType.RELEASE_KEYS -> Color(0xFF4CAF50)
    SignatureType.TEST_KEYS -> Color(0xFFFFC107)
    SignatureType.UNSIGNED -> Color(0xFFF44336)
    SignatureType.UNKNOWN -> Color(0xFF9E9E9E)
}
