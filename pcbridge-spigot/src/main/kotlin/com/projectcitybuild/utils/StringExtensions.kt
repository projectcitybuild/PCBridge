package com.projectcitybuild.utils

import java.util.regex.Pattern

/**
 * Converts a non-dash formatted UUID string into a
 * dash-formatted UUID string
 */
fun String.toDashFormattedUUID(): String {
    val pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
    return pattern.matcher(this).replaceAll("$1-$2-$3-$4-$5")
}
