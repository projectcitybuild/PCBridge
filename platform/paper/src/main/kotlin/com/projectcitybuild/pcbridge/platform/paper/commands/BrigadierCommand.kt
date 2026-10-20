package com.projectcitybuild.pcbridge.platform.paper.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

/**
 * Represents either a command or subcommand that can be
 * registered with Brigadier
 */
interface BrigadierCommand {
    val build: CommandDefinition

    val description: String?
        get() = build.description

    fun literal(): PaperCommandNode = build.node
}

fun Commands.register(command: BrigadierCommand) {
    val definition = command.build
    register(definition.node, definition.description)
}

fun JavaPlugin.registerCommands(vararg commands: BrigadierCommand) =
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commands.forEach { event.registrar().register(it) }
    }
