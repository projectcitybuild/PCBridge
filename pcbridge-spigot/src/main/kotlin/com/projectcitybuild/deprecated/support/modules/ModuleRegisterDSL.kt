// package com.projectcitybuild.support.modules
//
// import com.projectcitybuild.DependencyContainer
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
// import dev.jorel.commandapi.CommandAPICommand
// import org.bukkit.event.Event
//
// class ModuleRegisterDSL(
//     private val listenerRegistry: SpigotListenerRegistry,
//     val container: DependencyContainer,
// ) {
//     fun command(label: String, declaration: ModuleCommandBuilder) {
//         val command = CommandAPICommand(label)
//         declaration(command)
//         command.register()
//     }
//
//     fun <T: Event> listener(listener: SpigotListener<T>) {
//         listenerRegistry.register(listener)
//     }
// }
//
// private typealias ModuleCommandBuilder = CommandAPICommand.() -> Unit
// private typealias ModuleBuilder = ModuleRegisterDSL.() -> Unit
//
// typealias ModuleDeclaration = (ModuleBuilder) -> Unit
