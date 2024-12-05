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
    } catch (e: IllegalArgumentException) {
        // We only want to catch when the argument is missing, not when the input is invalid
        // (e.g. bad player name). Unfortunately Brigadier throws an [IllegalArgumentException]
        // in both cases, so the only workaround is to check the message...
        if (e.message == "No such argument '$name' exists on this command") {
            null
        } else {
            // Convert to an exception that automatically gets shown to the command sender
            throw IllegalStateException("Invalid argument for $name")
        }
    }
}