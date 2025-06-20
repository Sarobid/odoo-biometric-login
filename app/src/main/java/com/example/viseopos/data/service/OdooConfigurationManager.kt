package com.example.viseopos.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "odoo_settings")

object OdooConfigKeys {
    val HOSTNAME = stringPreferencesKey("hostname")
    val DBNAME = stringPreferencesKey("dbname")
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
}

class OdooConfigurationManager(private val context: Context) {
    val hostnameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OdooConfigKeys.HOSTNAME]
        }

    val dbnameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OdooConfigKeys.DBNAME]
        }

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OdooConfigKeys.USERNAME]
        }

    val passwordFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OdooConfigKeys.PASSWORD]
        }


    data class OdooConfig(
        val hostname: String? = null,
        val dbname: String? = null,
        val username: String? = null,
        val password: String? = null
    )

    val configFlow: Flow<OdooConfig> = context.dataStore.data
        .map { preferences ->
            OdooConfig(
                hostname = preferences[OdooConfigKeys.HOSTNAME],
                dbname = preferences[OdooConfigKeys.DBNAME],
                username = preferences[OdooConfigKeys.USERNAME],
                password = preferences[OdooConfigKeys.PASSWORD]
            )
        }

    suspend fun getCurrentConfiguration(): OdooConfig {
        return configFlow.first()
    }
    suspend fun saveHostname(hostname: String) {
        context.dataStore.edit { settings ->
            settings[OdooConfigKeys.HOSTNAME] = hostname
        }
    }

    suspend fun saveDbname(dbname: String) {
        context.dataStore.edit { settings ->
            settings[OdooConfigKeys.DBNAME] = dbname
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { settings ->
            settings[OdooConfigKeys.USERNAME] = username
        }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { settings ->
            settings[OdooConfigKeys.PASSWORD] = password
        }
    }

    suspend fun saveFullConfig(hostname: String, dbname: String, username: String, password: String?) {
        context.dataStore.edit { settings ->
            settings[OdooConfigKeys.HOSTNAME] = hostname
            settings[OdooConfigKeys.DBNAME] = dbname
            settings[OdooConfigKeys.USERNAME] = username
            if (password != null) {
                settings[OdooConfigKeys.PASSWORD] = password
            } else {

                settings.remove(OdooConfigKeys.PASSWORD)
            }
        }
    }
    suspend fun getLastPassword(): String?{
        return passwordFlow.first()
    }
    suspend fun getHostName(): String?{
        return hostnameFlow.first()
    }
    suspend fun getDbName(): String?{
        return dbnameFlow.first()
    }
    suspend fun getUsername(): String?{
        return usernameFlow.first()
    }
    suspend fun clearConfig() {
        context.dataStore.edit { settings ->
            settings.clear()
        }
    }
}