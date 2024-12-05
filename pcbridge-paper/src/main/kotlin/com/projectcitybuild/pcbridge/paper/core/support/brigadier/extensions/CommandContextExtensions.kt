package com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions

import com.mojang.brigadier.context.CommandContext

fun <S, V> CommandContext<S>.getOptionalArgument(name: String, clazz: Class<V>): V? {
    return try {
        getArgument(name, clazz)
    } catch (e: IllegalStateException) {
        null
    }
}