package com.example.viseopos.ui.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.viseopos.data.service.OdooRpcClientService
import com.example.viseopos.services.OdooConfigService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OdooAuthViewModel: ViewModel() {
    private val odooRpcClientService = OdooRpcClientService(OdooConfigService("http://192.168.129.68:8068","viseo_mars5"))
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token
    fun fetchTokenToMatricule(query: String = "") {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val result = odooRpcClientService.accessCodePin(
                    codePin = query,
                    usernameForAuth = "admin",
                    passwordForAuth = "p@5dM_"
                )
                result.onSuccess { token ->
                    Log.i("OdooAuthViewModel", "Token received: $token")
                    _token.value=token
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("OdooAuthViewModel", "Error in fetchPartners coroutine", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun consumeToken() {
        _token.value = null
    }
}
