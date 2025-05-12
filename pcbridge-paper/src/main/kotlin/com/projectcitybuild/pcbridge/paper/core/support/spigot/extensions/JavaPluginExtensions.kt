package com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions

import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.register
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

fun JavaPlugin.registerCommands(vararg commands: BrigadierCommand) = lifecycleManager
    .registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commands.forEach { event.registrar().register(it) }
    }
