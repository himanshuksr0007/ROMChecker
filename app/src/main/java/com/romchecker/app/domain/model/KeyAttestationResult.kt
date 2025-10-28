package com.romchecker.app.domain.model
enum class SecurityLevel { SOFTWARE, TRUSTED_ENVIRONMENT, STRONGBOX, UNKNOWN }
data class KeyAttestationResult(val isHardwareBacked: Boolean, val bootState: String, val bootloaderLocked: Boolean, val verifiedBootKey: String?, val certificates: List<String>, val securityLevel: SecurityLevel)
