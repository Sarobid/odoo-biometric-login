package com.example.viseopos.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viseopos.data.service.OdooConfigurationManager
import com.example.viseopos.data.service.OdooRpcClientService
import com.example.viseopos.services.OdooConfigService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class OdooAuthViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OdooAuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OdooAuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class OdooAuthViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val odooConfigurationManager = OdooConfigurationManager(application.applicationContext)
    private var odooRpcClientService: OdooRpcClientService? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()
    private val _isConfigReady = MutableStateFlow(false)
    val isConfigReady: StateFlow<Boolean> = _isConfigReady.asStateFlow()

    private val _hostname = MutableStateFlow("")
    val hostname: StateFlow<String> = _hostname.asStateFlow()

    init {
        loadConfigurationAndInitializeService()
    }

    private fun loadConfigurationAndInitializeService() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val config = odooConfigurationManager.getCurrentConfiguration()
                if (config.hostname != null && config.dbname != null && config.username != null && config.password != null) {
                    _hostname.value = config.hostname
                    val odooConfigService = OdooConfigService(
                        hostUrl = config.hostname,
                        dbName = config.dbname
                    )
                    odooRpcClientService = OdooRpcClientService(odooConfigService)
                    _isConfigReady.value = true
                    Log.i("OdooAuthViewModel", "Configuration loaded and OdooRpcClientService initialized.")
                } else {
                    Log.w("OdooAuthViewModel", "Odoo configuration is incomplete: $config")
                    _errorMessage.value = "Configuration Odoo incomplète. Veuillez vérifier les paramètres."
                    _isConfigReady.value = false
                    Log.w("OdooAuthViewModel", "Odoo configuration is incomplete: $config")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement de la configuration: ${e.localizedMessage}"
                Log.e("OdooAuthViewModel", "Error loading configuration", e)
                _isConfigReady.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTokenToMatricule(query: String) {
        if (!_isConfigReady.value || odooRpcClientService == null) {
            _errorMessage.value = "Service Odoo non prêt. Vérifiez la configuration."
            Log.w("OdooAuthViewModel", "fetchTokenToMatricule called but service is not ready.")
            return
        }
        if (query.isBlank()){
            _errorMessage.value = "Le code PIN ne peut pas être vide."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val currentAuthConfig = odooConfigurationManager.getCurrentConfiguration()

                if (currentAuthConfig.hostname == null || currentAuthConfig.username == null || currentAuthConfig.password == null) {
                    _errorMessage.value = "Identifiants d'authentification du bot manquants dans la configuration."
                    _isLoading.value = false
                    return@launch
                }

                val result = odooRpcClientService!!.accessCodePin(
                    codePin = query,
                    usernameForAuth = currentAuthConfig.username,
                    passwordForAuth = currentAuthConfig.password
                )
                _hostname.value = currentAuthConfig.hostname
                result.onSuccess { tokenValue ->
                    Log.i("OdooAuthViewModel", "Token received: $tokenValue")
                    _token.value = tokenValue
                }.onFailure { exception ->
                    Log.w("OdooAuthViewModel", "Authentication failed: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Erreur d'authentification inconnue"
                }
            } catch (e: Exception) {
                Log.e("OdooAuthViewModel", "Error in fetchTokenToMatricule coroutine", e)
                _errorMessage.value = "Erreur technique: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeToken() {
        _token.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}