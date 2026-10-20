package com.projectcitybuild.pcbridge.platform.paper.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

/**
 * Represents either a command or subcommand that can be
 * registered with Brigadier
 */
interface BrigadierCommand {
    val builder: CommandDefinition

    val description: String?
        get() = builder.description

    fun literal(): PaperCommandNode = builder.node
}

fun Commands.register(command: BrigadierCommand) {
    val definition = command.builder
    register(definition.node, definition.description)
}

fun JavaPlugin.registerCommands(vararg commands: BrigadierCommand) =
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commands.forEach { event.registrar().register(it) }
    }
