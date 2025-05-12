package com.projectcitybuild.pcbridge.paper.features.bans.utilities

import java.util.regex.Pattern

private object Regex {
    private const val ZERO_TO_255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
    private const val IP_REGEX = "$ZERO_TO_255\\.$ZERO_TO_255\\.$ZERO_TO_255\\.$ZERO_TO_255"

    val IP: Pattern
        get() = Pattern.compile(IP_REGEX)
}

fun isValidIP(string: String): Boolean {
    return Regex.IP.matcher(string).matches()
}
