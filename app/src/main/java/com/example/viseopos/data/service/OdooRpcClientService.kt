package com.example.viseopos.data.service

import android.util.Log
import com.example.viseopos.data.models.Partners
import com.example.viseopos.services.OdooConfigService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.xmlrpc.client.XmlRpcClient

class OdooRpcClientService(private val odooConfigService: OdooConfigService) {
    private var client = XmlRpcClient()

    private var internalUid: Int? = null
    private var internalPassword: String? = null
    private val TAG = "OdooRpcClientService"
    suspend fun authenticate(username: String, passwordIn: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val config = odooConfigService.getConfigCommon()
                val params = listOf(
                    odooConfigService.getCurrentDbName() ,
                    username,
                    passwordIn,
                    emptyMap<String, Any>()
                )
                val response = client.execute(config, "authenticate", params)
                if (response is Int && response != 0) {
                    internalUid = response
                    internalPassword = passwordIn
                    Log.i(TAG, "Authentication successful. UID: $internalUid")
                    Result.success(internalUid!!)
                } else {
                    Log.w(TAG, "Authentication failed. Response: $response")
                    reinitialisation()
                    Result.failure(Exception("Authentication failed. Invalid credentials or server error."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                reinitialisation()
                Result.failure(e)
            }
        }
    }

    suspend fun searchPartnersByName(searchQuery: String = "", usernameForAuth: String, passwordForAuth: String): Result<List<Partners>> {
        if (internalUid == null || internalPassword == null) {
            Log.d(TAG, "Not authenticated or password not stored, attempting authentication.")
            val authResult = authenticate(usernameForAuth, passwordForAuth)
            if (authResult.isFailure) {
                return Result.failure(authResult.exceptionOrNull() ?: Exception("Authentication required and failed."))
            }
        }
        return withContext(Dispatchers.IO) {
            try {
                val fields = listOf("id", "name", "email")
                val domain = if (searchQuery.isNotBlank()) {
                    listOf(listOf("name", "ilike", "%$searchQuery%"))
                } else {
                    emptyList<Any>()
                }
                val params = listOf(
                    odooConfigService.getCurrentDbName(),
                    internalUid,
                    internalPassword,
                    "res.partner",
                    "search_read",
                    listOf(domain),
                    mapOf("fields" to fields, "limit" to 10)
                )

                val config = odooConfigService.getConfigObject()
                val response = client.execute(config, "execute_kw", params)
                Log.d(TAG, "search_read 'res.partner' response: $response")
                if (response is Array<*>) {
                    val partners = response.mapNotNull { item ->
                        if (item is Map<*, *>) {
                            try {
                                val id = item["id"] as? Int ?: return@mapNotNull null
                                val name = item["name"] as? String
                                val email = item["email"] as? String
                                Log.v(TAG, "Mapped partner: ID=$id, Name=$name, Email=$email")
                                Partners(id, name, email)
                            } catch (e: ClassCastException) {
                                Log.e(TAG, "Error casting partner field for item: $item", e)
                                null
                            }
                        } else {
                            Log.w(TAG, "Item in response is not a Map: $item")
                            null
                        }
                    }
                    Result.success(partners)
                } else {
                    Log.e(TAG, "Unexpected response type for searchPartners: ${response?.javaClass?.name}")
                    Result.failure(Exception("Unexpected response from server while searching partners."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchPartners", e)
                Result.failure(e)
            }
        }
    }

    fun reinitialisation() {
        internalUid = null
        internalPassword = null
        Log.i(TAG, "User logged out, session data cleared.")
    }

    fun isAuthenticated(): Boolean {
        return internalUid != null && internalPassword != null
    }
}