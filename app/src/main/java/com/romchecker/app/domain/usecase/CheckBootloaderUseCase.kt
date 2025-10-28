package com.romchecker.app.domain.usecase
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.model.BootloaderResult
import kotlinx.coroutines.*

class CheckBootloaderUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(): BootloaderResult = withContext(Dispatchers.IO) {
        repository.checkBootloader()
    }
}
