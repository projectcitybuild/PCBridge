package com.projectcitybuild.pcbridge.platform.paper.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

/**
 * Represents either a command or subcommand that can be
 * registered with Brigadier
 */
interface BrigadierCommand {
    val description: String?
        get() = null

    fun literal(): PaperCommandNode
}

fun Commands.register(command: BrigadierCommand) = register(command.literal(), command.description)

fun JavaPlugin.registerCommands(vararg commands: BrigadierCommand) =
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commands.forEach { event.registrar().register(it) }
    }
