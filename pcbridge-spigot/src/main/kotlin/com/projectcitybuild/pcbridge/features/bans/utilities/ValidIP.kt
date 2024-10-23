package com.projectcitybuild.pcbridge.features.bans.utilities

import java.util.regex.Pattern

private object Regex {
    private val zeroTo255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
    private val ipRegex = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255"

    val IP: Pattern
        get() = Pattern.compile(ipRegex)
}

fun isValidIP(string: String): Boolean {
    return Regex.IP.matcher(string).matches()
}
