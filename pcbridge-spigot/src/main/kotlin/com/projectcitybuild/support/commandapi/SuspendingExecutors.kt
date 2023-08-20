package com.projectcitybuild.support.commandapi

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * Same as `executesPlayer` but with Kotlin coroutines support
 */
fun CommandAPICommand.suspendExecutesPlayer(
    plugin: JavaPlugin,
    executor: suspend (sender: Player, args: CommandArguments) -> Unit,
): CommandAPICommand {
    return executesPlayer(PlayerCommandExecutor { player, args ->
        plugin.launch {
            executor(player, args)
        }
    })
}