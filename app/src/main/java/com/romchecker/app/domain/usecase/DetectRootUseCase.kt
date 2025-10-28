package com.romchecker.app.domain.usecase
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.model.RootDetectionResult
import kotlinx.coroutines.*

class DetectRootUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(): RootDetectionResult = withContext(Dispatchers.IO) {
        repository.detectRoot()
    }
}
