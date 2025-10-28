package com.romchecker.app.domain.usecase
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.model.KeyAttestationResult
import kotlinx.coroutines.*

class PerformKeyAttestationUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(): KeyAttestationResult? = withContext(Dispatchers.IO) {
        try { repository.performKeyAttestation() } catch(e: Exception) { null }
    }
}
