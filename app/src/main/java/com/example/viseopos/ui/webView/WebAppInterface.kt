package com.example.viseopos.ui.webView

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.viseopos.data.models.Warehouse
import com.google.gson.Gson
class WebAppInterface(
    private val context: Context,
    private val currentSelectedWarehouses: SnapshotStateList<Warehouse>
) {

    private val gson = Gson()

    @JavascriptInterface
    fun getSelectedWarehouses(): String {
        try {
            val warehousesList = currentSelectedWarehouses.toList()
            Log.d("WebAppInterface", "getSelectedWarehouses called. Returning: $warehousesList")
            return gson.toJson(warehousesList)
        } catch (e: Exception) {
            Log.e("WebAppInterface", "Error serializing warehouses to JSON", e)
            return "[]"
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Log.d("WebAppInterface", "showToast called with message: $message")
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}