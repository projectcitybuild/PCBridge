package com.projectcitybuild.features.bans

class Sanitizer {

//    private val ipPortPattern = Regex(":[0-9]+$")
    private val ipPortPattern = Regex("^.*:[0-9]+$")

    /**
     * Sanitizes an IP fetched from a Bungeecord proxy by
     * stripping out slashes and the port if it exists
     *
     * eg. /127.0.0.1:1234 becomes 127.0.0.1
     */
    fun sanitizedIP(ip: String): String {
        var sanitized = ip.replace("/", "")

        if (sanitized.matches(ipPortPattern)) {
            val colonIndex = sanitized.indexOf(":")
            sanitized = sanitized.substring(0 until colonIndex)
        }

        return sanitized
    }
}
