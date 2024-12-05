package com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions

import com.mojang.brigadier.context.CommandContext

fun <S, T> CommandContext<S>.getArgument(
    name: String,
    clazz: Class<T>,
): T {
    return getArgument(name, clazz)
}

fun <S, T> CommandContext<S>.getOptionalArgument(
    name: String,
    clazz: Class<T>,
): T? {
    return try {
        getArgument(name, clazz)
    } catch (e: IllegalStateException) {
        null
    }
}