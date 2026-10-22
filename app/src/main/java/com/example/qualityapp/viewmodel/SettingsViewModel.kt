package com.example.qualityapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.TokenProvider
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TokenInfo(
    val source: String = "…",
    val length: Int = 0,
    val path: String = ""
)

data class SettingsUiState(
    val name: String = "",
    val number: String = "",
    val token: String = "",
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null,
    val tokenInfo: TokenInfo = TokenInfo()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.personnelName,
                prefs.personnelNumber,
                prefs.githubToken
            ) { n, num, t -> Triple(n, num, t) }
                .collect { (n, num, t) ->
                    _uiState.update { it.copy(name = n, number = num, token = t) }
                }
        }
        refreshTokenInfo()
    }

    fun onNameChange(v: String) =
        _uiState.update { it.copy(name = v, savedMessage = null, error = null) }

    fun onNumberChange(v: String) =
        _uiState.update { it.copy(number = v, savedMessage = null, error = null) }

    fun onTokenChange(v: String) =
        _uiState.update { it.copy(token = v, savedMessage = null, error = null) }

    fun refreshTokenInfo() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    tokenInfo = TokenInfo(
                        source = tokenProvider.sourceLabel(),
                        length = tokenProvider.tokenLength(),
                        path = tokenProvider.tokenFilePath()
                    )
                )
            }
        }
    }

    fun importTokenFromUri(uri: Uri) {
        if (_uiState.value.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, savedMessage = null) }
            val result = tokenProvider.importFromUri(uri)
            result.onSuccess { token ->
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        token = token,
                        savedMessage = "توکن از فایل بارگذاری شد (${token.length} کاراکتر)"
                    )
                }
                refreshTokenInfo()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = "بارگذاری ناموفق: ${e.message}"
                    )
                }
            }
        }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, savedMessage = null) }
            runCatching {
                prefs.savePersonnelInfo(_uiState.value.name, _uiState.value.number)
                prefs.saveToken(_uiState.value.token)
            }.onSuccess {
                _uiState.update {
                    it.copy(isSaving = false, savedMessage = "با موفقیت ذخیره شد")
                }
                refreshTokenInfo()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "خطا در ذخیره")
                }
            }
        }
    }
}