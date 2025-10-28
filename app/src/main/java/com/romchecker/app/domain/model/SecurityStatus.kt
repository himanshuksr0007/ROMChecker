package com.romchecker.app.domain.model
data class SecurityStatus(val playIntegrity: PlayIntegrityResult, val rootStatus: RootDetectionResult, val bootloaderStatus: BootloaderResult, val romSignature: ROMSignatureResult, val keyAttestation: KeyAttestationResult?, val timestamp: Long = System.currentTimeMillis())
