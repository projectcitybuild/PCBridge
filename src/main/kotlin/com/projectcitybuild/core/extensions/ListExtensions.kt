package com.projectcitybuild.core.extensions

fun List<String>.joinWithWhitespaces(range: IntRange): String? {
    if (this.size < range.first) {
        return null
    }
    return this.slice(range).joinToString(separator = " ")
}
