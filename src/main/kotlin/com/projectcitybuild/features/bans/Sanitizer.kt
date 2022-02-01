package com.projectcitybuild.features.bans

object Sanitizer {

    /**
     * Sanitizes an IP fetched from a Bungeecord proxy by
     * stripping out slashes and the port if it exists
     *
     * eg. /127.0.0.1:1234 becomes 127.0.0.1
     */
    fun sanitizedIP(ip: String): String {
        var sanitized = ip

        if (ip.startsWith("/")) {
            sanitized = sanitized.removePrefix("/")
        }

        if (ip.contains(":[0-9]+$")) {
            val colonIndex = sanitized.indexOf(":")
            sanitized = sanitized.substring(0..colonIndex)
        }

        return sanitized
    }
}