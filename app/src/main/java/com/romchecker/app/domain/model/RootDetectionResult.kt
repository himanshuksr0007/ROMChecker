package com.romchecker.app.domain.model
enum class RootType { MAGISK, KERNELSU, KITSUNE, SUPERSU, UNKNOWN, NONE }
data class RootDetectionResult(val isRooted: Boolean, val rootType: RootType, val isCustomROM: Boolean, val detectionMethods: List<DetectionMethod>)
data class DetectionMethod(val name: String, val detected: Boolean, val details: String)
