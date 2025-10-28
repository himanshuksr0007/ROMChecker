package com.romchecker.app.domain.usecase
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.model.ROMSignatureResult
import kotlinx.coroutines.*

class AnalyzeROMSignatureUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(): ROMSignatureResult = withContext(Dispatchers.IO) {
        repository.analyzeROMSignature()
    }
}
