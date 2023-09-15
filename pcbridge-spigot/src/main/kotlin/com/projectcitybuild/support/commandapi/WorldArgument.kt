package com.projectcitybuild.support.commandapi

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import org.bukkit.Bukkit
import org.bukkit.World

fun worldArgument(nodeName: String): Argument<World> {
    return CustomArgument(StringArgument(nodeName)) { info ->
        Bukkit.getWorld(info.input())
            ?: throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                CustomArgument.MessageBuilder("Unknown world: ").appendArgInput()
            )
    }.replaceSuggestions(ArgumentSuggestions.strings { _ ->
        Bukkit.getWorlds().map{ it.name }.toTypedArray()
    })
}