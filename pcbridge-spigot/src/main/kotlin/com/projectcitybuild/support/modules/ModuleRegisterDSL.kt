package com.projectcitybuild.support.modules

import com.projectcitybuild.DependencyContainer
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
import org.bukkit.event.Event

class ModuleRegisterDSL(
    private val commandRegistry: SpigotCommandRegistry,
    private val listenerRegistry: SpigotListenerRegistry,
    val container: DependencyContainer,
) {
    fun command(command: SpigotCommand) {
        commandRegistry.register(command)
    }

    fun <T: Event> listener(listener: SpigotListener<T>) {
        listenerRegistry.register(listener)
    }
}

private typealias ModuleBuilder = ModuleRegisterDSL.() -> Unit

typealias ModuleDeclaration = (ModuleBuilder) -> Unit
