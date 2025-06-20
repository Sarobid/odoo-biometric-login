package com.example.viseopos.services // Ou votre package

import android.util.Log
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.MalformedURLException
import java.net.URL

class OdooConfigService {
    private var host: String? = null
    private var database: String? = null
    private val TAG = "OdooConfigService"
    constructor(hostUrl: String?, dbName: String?){
        setHost(hostUrl)
        setDatabase(dbName)
    }
    fun setHost(hostUrl: String?) {
        this.host = hostUrl?.trim()
        Log.d(TAG, "Host set to: ${this.host}")
    }
    fun setDatabase(dbName: String?) {
        this.database = dbName?.trim()
        Log.d(TAG, "Database set to: ${this.database}")
    }


    @Throws(IllegalStateException::class, MalformedURLException::class)
    private fun getConfig(endpoint: String): XmlRpcClientConfigImpl {
        val currentHost = host
        if (currentHost.isNullOrBlank()) {
            throw IllegalStateException("Host is not configured or is blank in OdooConfigService.")
        }
        val serverUrlString = "${currentHost.trimEnd('/')}/xmlrpc/2/$endpoint"
        val serverUrl = URL(serverUrlString)
        val config = XmlRpcClientConfigImpl()
        config.serverURL = serverUrl
        config.isEnabledForExtensions = true
        Log.d(TAG, "XmlRpcClientConfig created for URL: ${config.serverURL}")
        return config
    }

    fun getConfigCommon(): XmlRpcClientConfigImpl {
        return getConfig("common")
    }
    fun getConfigObject(): XmlRpcClientConfigImpl {
        return getConfig("object")
    }

    fun getCurrentDbName(): String? {
        return database
    }

    fun isConfigured(): Boolean {
        return !host.isNullOrBlank() &&
                !database.isNullOrBlank()
    }

    fun clearConfiguration() {
        this.host = null
        this.database = null
        Log.d(TAG, "Configuration cleared.")
    }
}