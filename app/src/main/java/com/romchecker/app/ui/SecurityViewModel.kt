package com.romchecker.app.ui
import androidx.lifecycle.*
import com.romchecker.app.domain.model.*
import com.romchecker.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SecurityUiState {
    object Loading : SecurityUiState()
    data class Success(val securityStatus: SecurityStatus) : SecurityUiState()
    data class Error(val message: String) : SecurityUiState()
}

class SecurityViewModel(
    private val checkPlayIntegrityUseCase: CheckPlayIntegrityUseCase,
    private val detectRootUseCase: DetectRootUseCase,
    private val checkBootloaderUseCase: CheckBootloaderUseCase,
    private val analyzeROMSignatureUseCase: AnalyzeROMSignatureUseCase,
    private val performKeyAttestationUseCase: PerformKeyAttestationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<SecurityUiState>(SecurityUiState.Loading)
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init { performSecurityScan() }

    fun performSecurityScan() {
        viewModelScope.launch {
            _uiState.value = SecurityUiState.Loading
            try {
                val status = SecurityStatus(
                    checkPlayIntegrityUseCase(), detectRootUseCase(), 
                    checkBootloaderUseCase(), analyzeROMSignatureUseCase(),
                    performKeyAttestationUseCase()
                )
                _uiState.value = SecurityUiState.Success(status)
            } catch (e: Exception) {
                _uiState.value = SecurityUiState.Error(e.message ?: "Error")
            }
        }
    }
}
