package com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities

import java.net.InetAddress

class SpigotSanitizer private constructor() {
    companion object {
        private val ipPortPattern = Regex("^.*:[0-9]+$")

        /**
         * Sanitizes an IP provided by a Spigot server by
         * stripping out slashes and the port if it exists
         *
         * eg. /127.0.0.1:1234 becomes 127.0.0.1
         */
        fun ipAddress(ip: String): String {
            var sanitized = ip.replace("/", "")
            if (sanitized.matches(ipPortPattern)) {
                val colonIndex = sanitized.indexOf(":")
                sanitized = sanitized.substring(0 until colonIndex)
            }
            return sanitized
        }
    }
}

fun InetAddress.sanitized(): String {
    return SpigotSanitizer.ipAddress(address.toString())
}