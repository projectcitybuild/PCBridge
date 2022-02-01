package com.projectcitybuild.core

import java.util.regex.Pattern

object Regex {
    private val zeroTo255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
    private val ipRegex = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255(:[0-9]+)?"

    val IP: Pattern
        get() = Pattern.compile(ipRegex)
}