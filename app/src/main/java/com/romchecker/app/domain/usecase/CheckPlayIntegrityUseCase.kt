package com.romchecker.app.domain.usecase
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.model.PlayIntegrityResult
import kotlinx.coroutines.*

class CheckPlayIntegrityUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(): PlayIntegrityResult = withContext(Dispatchers.IO) {
        repository.checkPlayIntegrity()
    }
}
