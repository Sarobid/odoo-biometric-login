package com.example.viseopos.data.service

import android.util.Log
import com.example.viseopos.services.OdooConfigService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.xmlrpc.client.XmlRpcClient

class OdooRpcClientService(private val odooConfigService: OdooConfigService) {
    private var client = XmlRpcClient()

    private var internalUid: Int? = null
    private var internalPassword: String? = null
    private val TAG = "OdooRpcClientService"
    private val erreurConnection = "Pas de connexion Internet ou mauvaise paramètrage. Vérifiez et réessayez."
    suspend fun authenticate(username: String, passwordIn: String): Result<Int> {
        Log.i(TAG, "Authenticating with username: $username")
        Log.i(TAG, "Password: $passwordIn")
        return withContext(Dispatchers.IO) {
            try {
                val config = odooConfigService.getConfigCommon()
                val params = listOf(
                    odooConfigService.getCurrentDbName() ,
                    username,
                    passwordIn,
                    emptyMap<String, Any>()
                )
                Log.d(TAG, "Authentication parameters: $params")
                val response = client.execute(config, "authenticate", params)
                if (response is Int && response != 0) {
                    internalUid = response
                    internalPassword = passwordIn
                    Log.i(TAG, "Authentication successful. UID: $internalUid")
                    Result.success(internalUid!!)
                } else {
                    Log.w(TAG, "Authentication failed. Response: $response")
                    reinitialisation()
                    Result.failure(Exception(erreurConnection))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                reinitialisation()
                Result.failure(Exception(erreurConnection))
            }
        }
    }

    suspend fun accessCodePin(codePin: String = "", usernameForAuth: String, passwordForAuth: String): Result<String> {
        if (internalUid == null || internalPassword == null) {
            Log.d(TAG, "Not authenticated or password not stored, attempting authentication.")
            val authResult = authenticate(usernameForAuth, passwordForAuth)
            if (authResult.isFailure) {
                return Result.failure(authResult.exceptionOrNull() ?:Exception(erreurConnection))
            }
        }
        if(codePin.isBlank()){
            return Result.failure(Exception("Code Pin obligatoire"))
        }
        return withContext(Dispatchers.IO) {
            try {
                val domain = listOf(codePin)
                val params = listOf(
                    odooConfigService.getCurrentDbName(),
                    internalUid,
                    internalPassword,
                    "v.access.users.mobile",
                    "connect_to_mobile",
                    listOf(domain))
                val config = odooConfigService.getConfigObject()
                val response = client.execute(config, "execute_kw", params)
                val erreurStatic = "Le code PIN saisi est incorrect. Veuillez réessayer."
                if (response is Map<*, *>) {
                    val status = response["status"] as? Int
                    if (status == 200) {
                        val actualToken = response["token_mobile"] as? String
                        if (actualToken != null) {
                            Result.success(actualToken)
                        } else {
                            Result.failure(Exception(erreurStatic))
                        }
                    } else {
                        val errorMessage = response["error"] as? String ?: "Erreur inconnue du serveur Odoo."
                        Result.failure(Exception(erreurStatic))
                    }
                } else {
                    Result.failure(Exception(erreurStatic))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in accessCodePin", e)
                Result.failure(Exception(erreurConnection))
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