package com.projectcitybuild.core.extensions

fun Array<String>.joinWithWhitespaces(range: IntRange): String? {
    if (this.size > 1) {
        return null
    }
    return this.sliceArray(range).joinToString(separator = " ")
}