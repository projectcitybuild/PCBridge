// package com.projectcitybuild.modules.buildtools.invisframes
//
// import com.projectcitybuild.entities.Permissions
// import com.projectcitybuild.modules.buildtools.invisframes.commands.InvisFrameCommand
// import com.projectcitybuild.modules.buildtools.invisframes.listeners.FramePlaceListener
// import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemInsertListener
// import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemRemoveListener
// import com.projectcitybuild.support.modules.ModuleDeclaration
// import com.projectcitybuild.support.modules.PluginModule
// import dev.jorel.commandapi.arguments.MultiLiteralArgument
// import dev.jorel.commandapi.executors.PlayerCommandExecutor
//
// class InvisFramesModule: PluginModule {
//
//     override fun register(module: ModuleDeclaration) = module {
//         command("invisframe") {
//             withPermission(Permissions.COMMAND_BUILD_INVIS_FRAME)
//             withShortDescription("Gives you an invisible item frame")
//             withOptionalArguments(
//                 MultiLiteralArgument("type", listOf("glowing"))
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 val isGlowingFrame = args.get("type") == "glowing"
//
//                 InvisFrameCommand(
//                     container.spigotNamespace
//                 ).execute(player, isGlowingFrame)
//             })
//         }
//
//         listener(
//             FramePlaceListener(container.spigotNamespace),
//         )
//         listener(
//             ItemInsertListener(container.spigotNamespace),
//         )
//         listener(
//             ItemRemoveListener(container.spigotNamespace),
//         )
//     }
// }