package com.romchecker.app.domain.model
enum class BootloaderState { LOCKED, UNLOCKED, UNKNOWN }
enum class VerifiedBootState { GREEN, YELLOW, ORANGE, RED, UNKNOWN }
data class BootloaderResult(val state: BootloaderState, val verifiedBootState: VerifiedBootState, val details: String)
