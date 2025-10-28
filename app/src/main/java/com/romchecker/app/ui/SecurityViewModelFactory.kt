package com.romchecker.app.ui
import android.content.Context
import androidx.lifecycle.*
import com.romchecker.app.data.repository.SecurityRepository
import com.romchecker.app.domain.usecase.*

class SecurityViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = SecurityRepository(context)
        @Suppress("UNCHECKED_CAST")
        return SecurityViewModel(
            CheckPlayIntegrityUseCase(repo), DetectRootUseCase(repo),
            CheckBootloaderUseCase(repo), AnalyzeROMSignatureUseCase(repo),
            PerformKeyAttestationUseCase(repo)
        ) as T
    }
}
