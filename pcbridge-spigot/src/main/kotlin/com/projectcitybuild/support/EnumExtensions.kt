package com.projectcitybuild.support

import java.lang.Enum.valueOf

inline fun <reified T : Enum<T>> tryValueOf(type: String): T? {
    return try {
        valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}