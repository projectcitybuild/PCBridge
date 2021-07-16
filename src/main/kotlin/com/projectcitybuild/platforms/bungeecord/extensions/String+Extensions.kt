package com.projectcitybuild.platforms.bungeecord.extensions

import java.util.*
import java.util.regex.Pattern

private val UUID_WITHOUT_DASH_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")

fun String.addDashesToUUID(): String {
    val sanitisedString = this.replace("-", "")
    val matcher = UUID_WITHOUT_DASH_PATTERN.matcher(sanitisedString)

    return matcher.replaceAll("$1-$2-$3-$4-$5")
}