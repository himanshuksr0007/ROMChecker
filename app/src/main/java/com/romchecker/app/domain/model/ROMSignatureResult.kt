package com.romchecker.app.domain.model
enum class SignatureType { TEST_KEYS, RELEASE_KEYS, UNSIGNED, UNKNOWN }
data class ROMSignatureResult(val signatureType: SignatureType, val certificates: List<CertificateInfo>, val isValid: Boolean)
data class CertificateInfo(val issuer: String, val subject: String, val sha1Fingerprint: String, val sha256Fingerprint: String, val validFrom: Long, val validTo: Long, val algorithm: String)
