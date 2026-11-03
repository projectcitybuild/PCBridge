package com.projectcitybuild.pcbridge.platform.paper.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument

open class CommandArgument<T : Any>(
    val name: String,
    val type: ArgumentType<T>,
    internal val valueClass: Class<T>,
) {
    fun get(context: PaperCommandContext): T = context.getArgument(name, valueClass)

    fun getOptional(context: PaperCommandContext): T? = context.getOptionalArgument(name, valueClass)
}
