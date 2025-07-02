package com.example.viseopos.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viseopos.data.models.Warehouse
import com.example.viseopos.data.service.OdooConfigurationManager
import com.example.viseopos.data.service.OdooRpcClientService
import com.example.viseopos.data.service.WarehouseLocalService
import com.example.viseopos.services.OdooConfigService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ManageWarehouseViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageWarehouseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageWarehouseViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ManageWarehouseViewModel(application: Application) :  AndroidViewModel(application) {
    private val odooConfigurationManager = OdooConfigurationManager(application.applicationContext)
    private val warehouseLocalService = WarehouseLocalService(application.applicationContext)
    private var odooRpcClientService: OdooRpcClientService? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _isConfigReady = MutableStateFlow(false)
    val isConfigReady: StateFlow<Boolean> = _isConfigReady.asStateFlow()
    val warehouses = mutableStateListOf<Warehouse>()


    private val _availableWarehousesForSelection = MutableStateFlow<List<Warehouse>>(emptyList())
    val availableWarehousesForSelection: StateFlow<List<Warehouse>> = _availableWarehousesForSelection.asStateFlow()

    init {
        reloadWarehouses()
        loadConfigurationAndInitializeService()
        if (_isConfigReady.value) {
            loadListWarehouses()
        }
    }
    private fun loadConfigurationAndInitializeService() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val config = odooConfigurationManager.getCurrentConfiguration()
                if (config.hostname != null && config.dbname != null && config.username != null && config.password != null) {
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
    fun loadListWarehouses() {
        if (!_isConfigReady.value || odooRpcClientService == null) {
            _errorMessage.value = "Service Odoo non prêt. Vérifiez la configuration."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val currentAuthConfig = odooConfigurationManager.getCurrentConfiguration()

                if (currentAuthConfig.dbname == null || currentAuthConfig.hostname == null || currentAuthConfig.username == null || currentAuthConfig.password == null) {
                    _errorMessage.value = "Identifiants d'authentification du bot manquants dans la configuration."
                    _isLoading.value = false
                    return@launch
                }

                val result = odooRpcClientService!!.getAllDepot(
                    usernameForAuth = currentAuthConfig.username,
                    passwordForAuth = currentAuthConfig.password
                )
                result.onSuccess { odooServiceWarehouses ->
                    _availableWarehousesForSelection.value = odooServiceWarehouses.map { odooServiceWarehouse ->
                        Warehouse(
                            id = odooServiceWarehouse.id.toString(),
                            name = odooServiceWarehouse.name
                        )
                    }
                   }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Erreur d'authentification inconnue"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur technique: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSelectedWarehouse(warehouseToAdd: Warehouse) {
        viewModelScope.launch {
            val addedToLocal = warehouseLocalService.addWarehouse(warehouseToAdd)
        }
    }

    fun reloadWarehouses() {
        viewModelScope.launch {
            warehouseLocalService.selectedWarehousesFlow
                .catch { exception ->
                    Log.e("ManageWarehouseVM", "Error collecting selected warehouses", exception)
                    _errorMessage.value = "Erreur de chargement des dépôts locaux: ${exception.localizedMessage}"
                }
                .collect { locallySavedWarehouses ->
                    warehouses.clear()
                    warehouses.addAll(locallySavedWarehouses)
                    Log.i("ManageWarehouseVM", "Warehouses updated from local storage: $locallySavedWarehouses")
                }
        }
    }

    fun removeWarehouse(warehouse: Warehouse) {
        viewModelScope.launch {
            warehouseLocalService.removeWarehouse(warehouse)
        }
    }
}