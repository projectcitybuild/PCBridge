package com.projectcitybuild.core.extensions

fun Array<out String>.joinWithWhitespaces(range: IntRange): String? {
    if (this.size < range.first) {
        return null
    }
    return this.sliceArray(range).joinToString(separator = " ")
}