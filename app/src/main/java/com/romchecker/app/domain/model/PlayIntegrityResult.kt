package com.romchecker.app.domain.model
enum class IntegrityVerdict { MEETS_STRONG_INTEGRITY, MEETS_DEVICE_INTEGRITY, MEETS_BASIC_INTEGRITY, FAILED, UNKNOWN }
data class PlayIntegrityResult(val verdict: IntegrityVerdict, val deviceIntegrity: Boolean, val basicIntegrity: Boolean, val strongIntegrity: Boolean, val errorMessage: String? = null)
