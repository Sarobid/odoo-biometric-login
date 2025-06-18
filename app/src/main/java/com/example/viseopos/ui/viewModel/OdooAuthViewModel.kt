package com.example.viseopos.ui.viewModel

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.viseopos.data.service.OdooRpcClientService
import com.example.viseopos.services.OdooConfigService

class OdooAuthViewModel: ViewModel() {
    private val odooRpcClientService = OdooRpcClientService(OdooConfigService("http://192.168.32.143:8068","viseo_mars5"))
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _authenticationStatus = MutableLiveData<String>()
    val authenticationStatus: LiveData<String> = _authenticationStatus

    fun fetchPartners(query: String = "") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = odooRpcClientService.searchPartnersByName(
                    searchQuery = query,
                    usernameForAuth = "admin",
                    passwordForAuth = "p@5dM_"
                )
                result.onSuccess { partnerList ->
                    if (partnerList.isEmpty()) {
                        _authenticationStatus.value = if (query.isBlank()) "No partners found." else "No partners found for '$query'."
                    } else {
                        _authenticationStatus.value = "Fetched ${partnerList.size} partners."
                    }
                }.onFailure { exception ->
                    _authenticationStatus.value = "Failed to fetch partners: ${exception.message}"
                }
            } catch (e: Exception) {
                _authenticationStatus.value = "An unexpected error fetching partners: ${e.message}"
                Log.e("OdooAuthViewModel", "Error in fetchPartners coroutine", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
