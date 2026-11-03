package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import org.bukkit.GameMode

fun Argument.Companion.gameMode(name: String) = CommandArgument(
    name = name,
    type = EnumArgument(GameMode::class.java) {
        it.name.lowercase()
    },
    valueClass = GameMode::class.java,
)
