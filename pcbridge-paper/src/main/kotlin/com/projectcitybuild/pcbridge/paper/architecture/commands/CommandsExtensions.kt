package com.projectcitybuild.pcbridge.paper.architecture.commands

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

fun Commands.register(command: BrigadierCommand)
    = register(command.buildLiteral(), command.description)

fun JavaPlugin.registerCommands(vararg commands: BrigadierCommand) = lifecycleManager
    .registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commands.forEach { event.registrar().register(it) }
    }