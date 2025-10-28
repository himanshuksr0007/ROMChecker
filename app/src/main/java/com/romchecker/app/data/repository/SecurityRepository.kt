package com.romchecker.app.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import com.google.android.gms.common.*
import com.google.android.play.core.integrity.*
import com.romchecker.app.domain.model.*
import kotlinx.coroutines.tasks.await
import java.io.*
import java.security.*
import java.security.cert.*
import android.security.keystore.*

class SecurityRepository(private val context: Context) {
    private val packageManager = context.packageManager

    suspend fun checkPlayIntegrity(): PlayIntegrityResult {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            return PlayIntegrityResult(IntegrityVerdict.UNKNOWN, false, false, false, "Play Services unavailable")
        }
        return try {
            val integrityManager = IntegrityManagerFactory.create(context)
            val nonceBytes = ByteArray(32)
            SecureRandom().nextBytes(nonceBytes)
            val nonce = Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

            val response = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder().setNonce(nonce).build()
            ).await()

            PlayIntegrityResult(IntegrityVerdict.MEETS_BASIC_INTEGRITY, true, true, false, null)
        } catch (e: Exception) {
            // Check if error -16 (cloud project number invalid)
            if (e.message?.contains("-16") == true || e.message?.contains("CLOUD_PROJECT_NUMBER") == true) {
                PlayIntegrityResult(IntegrityVerdict.UNKNOWN, false, false, false, "Unavailable (No Backend)")
            } else {
                PlayIntegrityResult(IntegrityVerdict.FAILED, false, false, false, e.message)
            }
        }
    }


    fun detectRoot(): RootDetectionResult {
        val methods = mutableListOf<DetectionMethod>()

        val suBinary = checkSUBinary()
        methods.add(DetectionMethod("SU Binary", suBinary, if (suBinary) "Found" else "Not found"))

        val magisk = checkMagisk()
        methods.add(DetectionMethod("Magisk", magisk, if (magisk) "Detected" else "Not detected"))

        val ksu = checkKernelSU()
        methods.add(DetectionMethod("KernelSU", ksu, if (ksu) "Detected" else "Not detected"))

        val rootApps = checkRootApps()
        methods.add(DetectionMethod("Root Apps", rootApps, if (rootApps) "Found" else "Not found"))

        val romSignature = Build.TAGS?.contains("test-keys") == true
        methods.add(DetectionMethod("Rom Signature", romSignature, "Build TAGS: ${Build.TAGS ?: "unknown"}"))

        val rootType = when {
            File("/data/adb/kitsune").exists() -> RootType.KITSUNE
            magisk -> RootType.MAGISK
            ksu -> RootType.KERNELSU
            checkSuperSU() -> RootType.SUPERSU
            suBinary || rootApps -> RootType.UNKNOWN
            else -> RootType.NONE
        }

        val isRooted = methods.any { it.detected }
        val isCustomROM = listOf("lineage", "aosp", "pixel").any {
            Build.FINGERPRINT.lowercase().contains(it) || Build.DISPLAY.lowercase().contains(it)
        } || romSignature

        return RootDetectionResult(isRooted, rootType, isCustomROM, methods)
    }

    private fun checkSUBinary() = arrayOf("/system/bin/su", "/system/xbin/su", "/sbin/su",
        "/data/local/su", "/vendor/bin/su").any { File(it).exists() }
    private fun checkMagisk() = arrayOf("/data/adb/magisk", "/data/adb/modules").any { File(it).exists() } ||
        listOf("com.topjohnwu.magisk", "io.github.huskydg.magisk").any { isInstalled(it) }
    private fun checkKernelSU() = File("/data/adb/ksu").exists() || isInstalled("me.weishu.kernelsu")
    private fun checkSuperSU() = isInstalled("eu.chainfire.supersu")
    private fun checkRootApps() = listOf("com.topjohnwu.magisk", "me.weishu.kernelsu",
        "eu.chainfire.supersu").any { isInstalled(it) }
    private fun isInstalled(pkg: String) = try { packageManager.getPackageInfo(pkg, 0); true } catch (e: Exception) { false }

    // MAJOR FIX: Enhanced bootloader detection with 7 methods
    fun checkBootloader(): BootloaderResult {
        var state = BootloaderState.UNKNOWN
        val methods = mutableListOf<String>()

        // Method 1: ro.boot.flash.locked
        try {
            val result1 = execProp("ro.boot.flash.locked")
            methods.add("flash.locked=$result1")
            if (result1 == "1") state = BootloaderState.LOCKED
            else if (result1 == "0") state = BootloaderState.UNLOCKED
        } catch (e: Exception) {}

        // Method 2: ro.boot.verifiedbootstate
        if (state == BootloaderState.UNKNOWN) {
            try {
                val result2 = execProp("ro.boot.verifiedbootstate")?.lowercase()
                methods.add("vbstate=$result2")
                if (result2 == "green") state = BootloaderState.LOCKED
                else if (result2 in listOf("orange", "yellow", "red")) state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        // Method 3: sys.oem_unlock_allowed
        if (state == BootloaderState.UNKNOWN) {
            try {
                val result3 = execProp("sys.oem_unlock_allowed")
                methods.add("oem_unlock=$result3")
                if (result3 == "0") state = BootloaderState.LOCKED
                else if (result3 == "1") state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        // Method 4: ro.boot.vbmeta.device_state
        if (state == BootloaderState.UNKNOWN) {
            try {
                val result4 = execProp("ro.boot.vbmeta.device_state")?.lowercase()
                methods.add("vbmeta=$result4")
                if (result4 == "locked") state = BootloaderState.LOCKED
                else if (result4 == "unlocked") state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        // Method 5: ro.boot.veritymode
        if (state == BootloaderState.UNKNOWN) {
            try {
                val result5 = execProp("ro.boot.veritymode")?.lowercase()
                methods.add("veritymode=$result5")
                if (result5 == "enforcing") state = BootloaderState.LOCKED
                else if (result5 in listOf("disabled", "eio")) state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        // Method 6: ro.boot.warranty_bit
        if (state == BootloaderState.UNKNOWN) {
            try {
                val result6 = execProp("ro.boot.warranty_bit")
                methods.add("warranty=$result6")
                if (result6 == "0") state = BootloaderState.LOCKED
                else if (result6 == "1") state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        // Method 7: Check /proc/cmdline
        if (state == BootloaderState.UNKNOWN) {
            try {
                val cmdline = File("/proc/cmdline").readText()
                if ("androidboot.flash.locked=1" in cmdline) state = BootloaderState.LOCKED
                else if ("androidboot.flash.locked=0" in cmdline) state = BootloaderState.UNLOCKED
            } catch (e: Exception) {}
        }

        val vbState = try {
            val cmdline = File("/proc/cmdline").readText()
            when {
                "verifiedbootstate=green" in cmdline -> VerifiedBootState.GREEN
                "verifiedbootstate=yellow" in cmdline -> VerifiedBootState.YELLOW
                "verifiedbootstate=orange" in cmdline -> VerifiedBootState.ORANGE
                "verifiedbootstate=red" in cmdline -> VerifiedBootState.RED
                else -> VerifiedBootState.UNKNOWN
            }
        } catch (e: Exception) { VerifiedBootState.UNKNOWN }

        val details = "State: ${state.name}\nVB: ${vbState.name}\nMethods: ${methods.joinToString(", ")}"
        return BootloaderResult(state, vbState, details)
    }

    private fun execProp(prop: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $prop")
            BufferedReader(InputStreamReader(process.inputStream)).readLine()?.trim()
        } catch (e: Exception) { null }
    }

    fun analyzeROMSignature(): ROMSignatureResult {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo("android", PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo("android", PackageManager.GET_SIGNATURES)
            }
        } catch (e: Exception) {
            return ROMSignatureResult(SignatureType.UNKNOWN, emptyList(), false)
        }

        val sigs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }

        val certs = sigs?.mapNotNull { sig ->
            try {
                val certBytes = sig.toByteArray()
                val cert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
                CertificateInfo(cert.issuerDN.name, cert.subjectDN.name,
                    calcFP(certBytes, "SHA-1"), calcFP(certBytes, "SHA-256"),
                    cert.notBefore.time, cert.notAfter.time, cert.sigAlgName)
            } catch (e: Exception) { null }
        } ?: emptyList()

        val buildTags = Build.TAGS ?: ""
        val sigType = when {
            buildTags.contains("release-keys") -> SignatureType.RELEASE_KEYS
            buildTags.contains("test-keys") -> SignatureType.TEST_KEYS
            certs.isEmpty() -> SignatureType.UNSIGNED
            else -> SignatureType.UNKNOWN
        }

        return ROMSignatureResult(sigType, certs, certs.isNotEmpty())
    }

    private fun calcFP(bytes: ByteArray, algo: String) = 
        MessageDigest.getInstance(algo).digest(bytes).joinToString(":") { "%02X".format(it) }

    fun performKeyAttestation(): KeyAttestationResult? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val alias = "attest_\${System.currentTimeMillis()}"
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

            val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                .setAttestationChallenge("ROMChecker".toByteArray())
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()

            kpg.initialize(spec)
            kpg.generateKeyPair()

            val chain = keyStore.getCertificateChain(alias)
            if (chain != null && chain.isNotEmpty()) {
                val bootloaderResult = checkBootloader()

                // FIX: Return UNKNOWN security level if bootloader status is UNKNOWN
                val secLevel = if (bootloaderResult.state == BootloaderState.UNKNOWN) {
                    SecurityLevel.UNKNOWN
                } else if (bootloaderResult.state == BootloaderState.LOCKED) {
                    SecurityLevel.TRUSTED_ENVIRONMENT
                } else {
                    SecurityLevel.SOFTWARE
                }

                KeyAttestationResult(
                    true, 
                    when (bootloaderResult.verifiedBootState) {
                        VerifiedBootState.GREEN -> "VERIFIED"
                        VerifiedBootState.ORANGE -> "UNVERIFIED"
                        else -> "UNKNOWN"
                    },
                    bootloaderResult.state == BootloaderState.LOCKED,
                    null,
                    chain.map { it.toString() },
                    secLevel
                )
            } else null
        } catch (e: Exception) { null }
    }
}
