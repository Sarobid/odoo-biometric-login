package com.example.viseopos.data.service

import android.content.Context
import androidx.compose.ui.input.key.type
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.viseopos.data.models.Warehouse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.selectedWarehousesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_selected_warehouses")

class WarehouseLocalService(private val context: Context) {

    private val gson = Gson()

    private companion object {
        val SELECTED_WAREHOUSES_KEY = stringPreferencesKey("selected_warehouses_json")
    }
    val selectedWarehousesFlow: Flow<List<Warehouse>> = context.selectedWarehousesDataStore.data
        .map { preferences ->
            val jsonString = preferences[SELECTED_WAREHOUSES_KEY]
            if (jsonString != null) {
                try {
                    val type = object : TypeToken<List<Warehouse>>() {}.type
                    gson.fromJson<List<Warehouse>>(jsonString, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

    suspend fun saveSelectedWarehouses(warehouses: List<Warehouse>) {
        try {
            val jsonString = gson.toJson(warehouses)
            context.selectedWarehousesDataStore.edit { preferences ->
                preferences[SELECTED_WAREHOUSES_KEY] = jsonString
            }
        } catch (e: Exception) {
            throw IOException("Failed to save warehouses", e)
        }
    }

    suspend fun addWarehouse(warehouseToAdd: Warehouse): Boolean {
        var wasAdded = false
        context.selectedWarehousesDataStore.edit { preferences ->
            val jsonString = preferences[SELECTED_WAREHOUSES_KEY]
            val currentList: MutableList<Warehouse> = if (jsonString != null) {
                try {
                    val type = object : TypeToken<MutableList<Warehouse>>() {}.type
                    gson.fromJson(jsonString, type) ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            if (currentList.none { it.name == warehouseToAdd.name }) {
                currentList.add(warehouseToAdd)
                preferences[SELECTED_WAREHOUSES_KEY] = gson.toJson(currentList)
                wasAdded = true
            }
        }
        return wasAdded
    }

    suspend fun removeWarehouse(warehouseToRemove: Warehouse) {
        context.selectedWarehousesDataStore.edit { preferences ->
            val jsonString = preferences[SELECTED_WAREHOUSES_KEY]
            if (jsonString != null) {
                try {
                    val type = object : TypeToken<MutableList<Warehouse>>() {}.type
                    val currentList: MutableList<Warehouse> = gson.fromJson(jsonString, type) ?: mutableListOf()
                    if (currentList.removeAll { it.name == warehouseToRemove.name }) {
                        preferences[SELECTED_WAREHOUSES_KEY] = gson.toJson(currentList)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    suspend fun clearAllSelectedWarehouses() {
        context.selectedWarehousesDataStore.edit { preferences ->
            preferences.remove(SELECTED_WAREHOUSES_KEY)
        }
    }
}