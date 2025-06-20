package com.example.viseopos.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viseopos.data.service.OdooConfigurationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class SettingViewModel(application: Application) : AndroidViewModel(application) {
    private val odooConfigurationManager = OdooConfigurationManager(application.applicationContext)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _hostname = MutableStateFlow("")
    val hostname: StateFlow<String> = _hostname.asStateFlow()

    private val _dbName = MutableStateFlow("")
    val dbName: StateFlow<String> = _dbName.asStateFlow()

    private val _botUsername = MutableStateFlow("")
    val botUsername: StateFlow<String> = _botUsername.asStateFlow()

    private val _botPassword = MutableStateFlow("")
    val botPassword: StateFlow<String> = _botPassword.asStateFlow()

    init {
        loadInitialConfiguration()
    }

    private fun loadInitialConfiguration() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentConfig = odooConfigurationManager.getCurrentConfiguration()
                _hostname.value = currentConfig.hostname ?: ""
                _dbName.value = currentConfig.dbname ?: ""
                _botUsername.value = currentConfig.username ?: ""
                _botPassword.value = ""
            } catch (e: Exception) {
                _snackbarMessage.value = "Erreur chargement config: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onHostnameChanged(newHostname: String) {
        _hostname.value = newHostname
        clearSnackbarMessage()
    }

    fun onDbNameChanged(newDbName: String) {
        _dbName.value = newDbName
        clearSnackbarMessage()
    }

    fun onBotUsernameChanged(newUsername: String) {
        _botUsername.value = newUsername
        clearSnackbarMessage()
    }

    fun onBotPasswordChanged(newPassword: String) {
        _botPassword.value = newPassword
        clearSnackbarMessage()
    }

    fun saveConfiguration() {
        if (_hostname.value.isBlank() || _dbName.value.isBlank() || _botUsername.value.isBlank()) {
            _snackbarMessage.value = "Hostname, base de données et nom d'utilisateur sont requis."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var password = _botPassword.value
                if( _botPassword.value.isBlank() && odooConfigurationManager.getLastPassword().isNullOrBlank()){
                    throw Exception("Le mot de passe est requis.")
                }
                if( _botPassword.value.isBlank()){
                    password = odooConfigurationManager.getLastPassword().toString();
                }
                odooConfigurationManager.saveFullConfig(
                    _hostname.value.trim(),
                    _dbName.value.trim(),
                    _botUsername.value.trim(),
                    password
                )
                _snackbarMessage.value = "Paramètres sauvegardés avec succès !"
            } catch (e: Exception) {
                _snackbarMessage.value = "Erreur sauvegarde: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}