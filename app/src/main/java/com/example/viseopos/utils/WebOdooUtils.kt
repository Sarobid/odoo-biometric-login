package com.example.viseopos.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WebOdooUtils {
    fun isDeconnected(url: String): Boolean{
        var est = false
        if(url.indexOf("login") > -1) {
            est = true
        }
        return est
    }
    fun encodeHostname(hostname: String): String {
        return URLEncoder.encode(hostname, StandardCharsets.UTF_8.toString())
    }
    fun decodeHostname(encodedHostname: String): String {
        return URLDecoder.decode(encodedHostname, StandardCharsets.UTF_8.toString())
    }
}