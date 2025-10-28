# ROMChecker
``` This readme file and some code for this project (~10%) is created by taking help from AI. ```

A comprehensive, real-time security analysis tool for custom ROM developers, enthusiasts, and power users. ROMChecker provides deep device security insights including integrity verification, root detection, bootloader analysis, and ROM signature inspection.

## 📖 Table of Contents

- [Project Overview](#project-overview)
- [Goal & Purpose](#goal--purpose)
- [Why Play Integrity Doesn't Work Out of the Box](#why-play-integrity-doesnt-work-out-of-the-box)
- [Features](#features)
- [Technical Architecture](#technical-architecture)
- [Contributing & Forking](#contributing--forking)
- [Play Integrity Setup (Optional)](#play-integrity-setup-optional)

---

## Project Overview

ROMChecker is an Android application built with **Kotlin** and **Jetpack Compose** that performs comprehensive device security analysis. It's designed specifically for the custom ROM community to verify device integrity, identify root access methods, analyze ROM signatures, and inspect hardware security features.
---

## Goal & Purpose

### Primary Goals:

1. **Security Verification** - Help custom ROM developers and users verify device security status without relying on external services
2. **Root Detection** - Accurately identify if a device is rooted and determine the specific root method (Magisk, KernelSU, etc.)
3. **ROM Analysis** - Detect whether a ROM is signed with test keys or release keys
4. **Educational Tool** - Provide transparency about device security mechanisms and bootloader status
5. **Development Aid** - Assist ROM developers in testing and validating their builds before release

### Target Audience:

- Custom ROM developers (LineageOS, AOSP, Pixel Experience, etc.)
- Android enthusiasts and power users
- ROM testers and quality assurance teams

---

## Why Play Integrity Doesn't Work Out of the Box?

- **Requires Google Cloud Project with billing enabled** And I don't have anything to enable billing 😭 

---

## Features

### 1. Play Integrity Analysis
- Requests integrity tokens from Google Play Services
- Displays device integrity status
- Shows error messages if backend not configured
- Requires: Google Play Services installed
- Status: Works with backend, shows "Unavailable (No Backend)" without it

### 2. Root Detection (8 Methods)
**Detection Methods:**
- SU Binary scanning in system paths
- Root management app detection (Magisk, KernelSU, SuperSU, Xposed)
- Build tags analysis (`ro.build.tags`)
- System properties inspection
- Busybox detection
- Read-write system partition checking
- Magisk-specific module detection
- Xposed framework detection

**Identifies Specific Root Type:**
- **Magisk** - Modern systemless root
- **KernelSU** - Kernel-level root access
- **Kitsune** - Kitsune Magisk variant
- **SuperSU** - Legacy root solution
- **Unknown** - Rooted but method unidentified
- **None** - Not rooted (or undetected)


### 3. Bootloader Status Detection (7 Methods)
**Detection Approaches:**
1. `ro.boot.flash.locked` property
2. `ro.boot.verifiedbootstate` property
3. `sys.oem_unlock_allowed` property
4. `ro.boot.vbmeta.device_state` property
5. `ro.boot.veritymode` property
6. `ro.boot.warranty_bit` property
7. `/proc/cmdline` file parsing

**Returns:**
- **LOCKED** - Bootloader is locked
- **UNLOCKED** - Bootloader is unlocked
- **UNKNOWN** - Cannot determine (device doesn't expose properties)

**Verified Boot States:**
- **GREEN** - Fully verified and locked
- **YELLOW** - Verified with custom key
- **ORANGE** - Unlocked bootloader detected
- **RED** - Verification failed
- **UNKNOWN** - Cannot determine

**Notes:** Detection accuracy depends on device manufacturer implementation. Some devices don't expose bootloader properties without root permissions.

### 4. ROM Signature Analysis
- Queries system "android" package certificates
- Detects signing key type
- Extracts certificate information
- Calculates fingerprints

**Signature Types:**
- **RELEASE_KEYS** - Production ROM (secure)
- **TEST_KEYS** - Development/test ROM
- **UNSIGNED** - No signature found
- **UNKNOWN** - Cannot determine

**Certificate Details:**
- Issuer and Subject information
- SHA-1 and SHA-256 fingerprints
- Algorithm information
- Validity period (from/to dates)
- One-tap copy to clipboard


### 5. Key Attestation (Hardware Security)
- Hardware-backed key support detection
- Boot state verification
- Bootloader lock status confirmation
- Security level determination

**Security Levels:**
- **STRONGBOX** - Highest security (dedicated hardware security module)
- **TRUSTED_ENVIRONMENT** - High security (TEE/Secure Enclave)
- **SOFTWARE** - Software-based key storage
- **UNKNOWN** - Cannot determine or unavailable

**Features:**
- Expandable certificate chain viewer
- Boot state display
- Bootloader lock status sync with detection
- Returns UNKNOWN when bootloader status cannot be determined

### 6. Custom ROM Detection
- Identifies common custom ROMs (LineageOS, AOSP, Pixel Experience, etc.)
- Analyzes build fingerprints and display information
- Cross-references with test keys detection
- Displays "Custom ROM" indicator when detected

### 7. Export & Share Functionality
- JSON export of complete scan results
- Popup preview of export data
- Share functionality to any app
- Save to device storage
- Includes:
  - Timestamp
  - All detection results
  - Play Integrity status
  - Root detection details
  - Bootloader information
  - ROM signature data
  - Key attestation results


---

## Technical Architecture

### Technology Stack

**Language & Framework:**
- **Kotlin 1.9.20** - Modern Android development language
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - Latest Google design system
- **MVVM Pattern** - Separation of concerns
- **Clean Architecture** - Layered design

**Minimum Requirements:**
- Android 8.0 (API 26)
- Google Play Services (for Play Integrity)

**Key Dependencies:**
- Play Integrity API 1.3.0
- Google Play Services Base 18.4.0
- Kotlin Coroutines 1.7.3
- GSON 2.10.1 (for JSON export)
- BouncyCastle (for cryptography)

---

## Contributing & Forking

### How to Improve This Project

This project is designed to be extended and improved by the community. Here are common contribution areas:

### 1. Add More Root Detection Methods

**File:** `app/src/main/java/com/romchecker/app/data/repository/SecurityRepository.kt`

**Method:** Add new detection in `detectRoot()` function:

```kotlin
fun detectRoot(): RootDetectionResult {
    val methods = mutableListOf<DetectionMethod>()
    
    // Add your new detection here
    val customDetection = checkCustomRootMethod()
    methods.add(DetectionMethod(
        name = "Custom Root Method",
        detected = customDetection,
        details = if (customDetection) "Found" else "Not found"
    ))
    
    // ... rest of code
}

private fun checkCustomRootMethod(): Boolean {
    // Implement your detection logic
    return File("/path/to/check").exists()
}
```

### 2. Improve Bootloader Detection

**File:** `SecurityRepository.kt` - `checkBootloader()` function

Add new property checks:
```kotlin
// Add new method to the detection chain
try {
    val result = execProp("ro.your.property")
    if (result == "expected_value") state = BootloaderState.LOCKED
} catch (e: Exception) {}
```

### 3. Add Backend Integration for Play Integrity

**File:** Create new `app/src/main/java/com/romchecker/app/data/remote/PlayIntegrityService.kt`

```kotlin
class PlayIntegrityService(private val backendUrl: String) {
    suspend fun verifyToken(token: String): IntegrityVerdict {
        // Call your backend API with token
        // Parse verdict from response
        return fetchVerdictFromBackend(token)
    }
}
```

### 4. Add More Export Formats

**File:** `SettingsScreen.kt` - `generateExportJson()` function

Add CSV, XML, or HTML export:
```kotlin
private fun generateExportCsv(status: SecurityStatus): String {
    // Generate CSV format
    return buildString {
        append("Feature,Status,Details\n")
        append("Play Integrity,${status.playIntegrity.verdict},...\n")
        // ... etc
    }
}
```

### 5. Add Cloud Storage Integration

Implement Google Drive or Firebase backup:
```kotlin
class CloudBackupService {
    suspend fun uploadScanResults(json: String): Boolean {
        // Upload to cloud storage
        return true
    }
}
```

### 6. Add Historical Data Tracking

Track device security over time:
```kotlin
class ScanHistoryRepository {
    suspend fun saveScan(status: SecurityStatus)
    suspend fun getHistory(): List<SecurityStatus>
    suspend fun getChangesSince(date: Long): List<Change>
}
```

### 7. Improve UI/UX

- Add graphs/charts for historical data
- Implement dark mode transitions
- Add animations for status changes
- Create custom status indicators
- Improve accessibility


---

## Play Integrity Setup (Optional)

### For Users Who Want Full Play Integrity Working

If you want actual Play Integrity verdicts instead of "Unavailable (No Backend)", you'll need to set up a backend server. This is **optional** and **not required** for the app to work.

### Requirements

1. Google Cloud Project with billing enabled (Free 10,000 requests per day but requires billing to be enabled)
2. Backend server (can be Firebase, Cloud Run, or your own server)
3. Play Integrity API enabled in Google Cloud

### Step-by-Step Setup

#### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create new project: **New Project** → Name: "ROMChecker" → Create
3. Enable billing: **Billing** → Link billing account (required for Play Integrity)
4. Note your **Project Number** (not Project ID)

#### Step 2: Enable Play Integrity API

1. Search for "Play Integrity API"
2. Click on it and press **Enable**
3. Go to **Credentials** → **Create Credentials** → **Service Account**
4. Name: "romchecker-service"
5. Download the JSON key file (keep it safe!)

#### Step 3: Update AndroidManifest.xml (Optional Meta-Data)

```xml
<application>
    <!-- Optional: Add project number as meta-data -->
    <meta-data
        android:name="com.google.play.integrity.project_number"
        android:value="123456789" />
    <!-- ... rest of application ... -->
</application>
```

Replace `123456789` with your actual Cloud Project Number.

#### Step 4: Update SecurityRepository.kt

**File:** `SecurityRepository.kt`

Modify the `checkPlayIntegrity()` function to include project number:

```kotlin
suspend fun checkPlayIntegrity(): PlayIntegrityResult {
    // ... existing code ...
    
    val response = integrityManager.requestIntegrityToken(
        IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .setCloudProjectNumber(123456789L)  // Add your project number here
            .build()
    ).await()
    
    // ... rest of code ...
}
```

#### Step 5: Set Up Backend Verification Server

This requires running a backend that verifies tokens. Options:

**Option A: Firebase Cloud Functions** (Easy)

```javascript
// functions/index.js
const functions = require('firebase-functions');
const axios = require('axios');

exports.verifyIntegrityToken = functions.https.onCall(async (data, context) => {
    const token = data.token;
    
    // Call Google's verification API
    const response = await axios.post(
        'https://playintegrity.googleapis.com/v1/decodeIntegrityToken',
        { token: token },
        { headers: { 'Authorization': `Bearer ${serviceAccountToken}` } }
    );
    
    return { verdict: response.data.verdictToken };
});
```

**Option B: Google Cloud Run** (Serverless)

Deploy containerized verification service without managing servers.

#### Step 6: Update App to Use Backend

Modify `SecurityRepository.kt` to send token to backend:

```kotlin
suspend fun checkPlayIntegrity(): PlayIntegrityResult {
    // ... get token from integrityManager ...
    
    return try {
        val response = integrityManager.requestIntegrityToken(
            IntegrityTokenRequest.builder().setNonce(nonce).build()
        ).await()
        
        val token = response.token()
        
        // Send token to your backend for verification
        val verdict = verifyTokenWithBackend(token)
        
        PlayIntegrityResult(verdict, true, true, true, null)
    } catch (e: Exception) {
        // ... error handling ...
    }
}

private suspend fun verifyTokenWithBackend(token: String): IntegrityVerdict {
    // Call your backend API
    // Example using Retrofit or HttpClient
    val response = httpClient.post("https://your-backend.com/verify") {
        parameter("token", token)
    }
    
    return when (response.verdict) {
        "MEETS_STRONG_INTEGRITY" -> IntegrityVerdict.MEETS_STRONG_INTEGRITY
        "MEETS_DEVICE_INTEGRITY" -> IntegrityVerdict.MEETS_DEVICE_INTEGRITY
        "MEETS_BASIC_INTEGRITY" -> IntegrityVerdict.MEETS_BASIC_INTEGRITY
        else -> IntegrityVerdict.FAILED
    }
}
```


### Troubleshooting

**Error -16: CLOUD_PROJECT_NUMBER_IS_INVALID**
- Check project number is correct
- Ensure billing is enabled
- Verify API is enabled

**Error -13: NONCE_NOT_BASE64**
- Nonce encoding is already fixed in app
- Shouldn't occur with current version

**Error -2: PLAY_SERVICES_NOT_AVAILABLE**
- Install Google Play Services
- Update to latest version
- May not work on some devices/ROMs

**Backend returning 401 Unauthorized**
- Verify service account key is valid
- Check token expiration
- Ensure CORS is configured (if browser access)

---

## License & Attribution

This project is open-source and available for community contributions. When forking or using this code:

1. Maintain attribution to original developers
2. Provide documentation of changes
3. Share improvements back to community
4. Respect Google Play's terms of service
5. Use Play Integrity API responsibly

---

