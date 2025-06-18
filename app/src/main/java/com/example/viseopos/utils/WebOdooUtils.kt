package com.example.viseopos.utils

object WebOdooUtils {
    fun isDeconnected(url: String): Boolean{
        var est = false
        if(url.indexOf("login") > -1) {
            est = true
        }
        return est
    }
}