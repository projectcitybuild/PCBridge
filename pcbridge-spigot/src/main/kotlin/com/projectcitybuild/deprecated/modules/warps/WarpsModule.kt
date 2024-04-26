// package com.projectcitybuild.modules.warps
//
// import com.projectcitybuild.entities.Permissions
// import com.projectcitybuild.modules.warps.actions.CreateWarp
// import com.projectcitybuild.modules.warps.actions.DeleteWarp
// import com.projectcitybuild.modules.warps.actions.GetWarpList
// import com.projectcitybuild.modules.warps.actions.TeleportToWarp
// import com.projectcitybuild.modules.warps.commands.DeleteWarpCommand
// import com.projectcitybuild.modules.warps.commands.CreateWarpCommand
// import com.projectcitybuild.modules.warps.commands.WarpCommand
// import com.projectcitybuild.modules.warps.commands.ListWarpsCommand
// import com.projectcitybuild.support.commandapi.suspendExecutesPlayer
// import com.projectcitybuild.support.modules.ModuleDeclaration
// import com.projectcitybuild.support.modules.PluginModule
// import dev.jorel.commandapi.arguments.ArgumentSuggestions
// import dev.jorel.commandapi.arguments.IntegerArgument
// import dev.jorel.commandapi.arguments.StringArgument
// import dev.jorel.commandapi.executors.PlayerCommandExecutor
//
// class WarpsModule: PluginModule {
//     override fun register(module: ModuleDeclaration) = module {
//         command("delwarp") {
//             withPermission(Permissions.COMMAND_WARPS_DELETE)
//             withShortDescription("Deletes a warp")
//             withArguments(
//                 StringArgument("warp")
//                     .replaceSuggestions(
//                         ArgumentSuggestions.strings(container.warpRepository.names())
//                     ),
//             )
//             suspendExecutesPlayer(container.plugin) { player, args ->
//                 DeleteWarpCommand(
//                     DeleteWarp(
//                         container.warpRepository,
//                         container.localEventBroadcaster,
//                     ),
//                 ).execute(
//                     commandSender = player,
//                     warpName = args.get("warp") as String,
//                 )
//             }
//         }
//
//         command("setwarp") {
//             withPermission(Permissions.COMMAND_WARPS_CREATE)
//             withShortDescription("Creates a warp at the current position and direction")
//             withArguments(
//                 StringArgument("warp")
//                     .replaceSuggestions(
//                         ArgumentSuggestions.strings(container.warpRepository.names())
//                     ),
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 CreateWarpCommand(
//                     CreateWarp(
//                         container.warpRepository,
//                         container.localEventBroadcaster,
//                         container.time,
//                     )
//                 ).execute(
//                     commandSender = player,
//                     warpName = args.get("warp") as String,
//                 )
//             })
//         }
//
//         command("warps") {
//             withPermission(Permissions.COMMAND_WARPS_LIST)
//             withShortDescription("Gets a list of all warps available")
//             withOptionalArguments(
//                 IntegerArgument("page"),
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 ListWarpsCommand(
//                     GetWarpList(
//                         container.warpRepository,
//                         container.config,
//                     )
//                 ).execute(
//                     commandSender = player,
//                     pageIndex = args.get("page") as Int?,
//                 )
//             })
//         }
//
//         command("warp") {
//             withPermission(Permissions.COMMAND_WARPS_USE)
//             withShortDescription("Teleports to a pre-defined location")
//             withArguments(
//                 StringArgument("warp")
//                     .replaceSuggestions(
//                         ArgumentSuggestions.strings(container.warpRepository.names())
//                     ),
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 WarpCommand(
//                     TeleportToWarp(
//                         container.warpRepository,
//                         container.nameGuesser,
//                         container.logger,
//                         container.localEventBroadcaster,
//                         container.server,
//                     ),
//                 ).execute(
//                     commandSender = player,
//                     warpName = args.get("warp") as String,
//                 )
//             })
//         }
//     }
// }