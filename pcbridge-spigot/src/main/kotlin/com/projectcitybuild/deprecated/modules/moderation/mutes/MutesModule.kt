// package com.projectcitybuild.modules.moderation.mutes
//
// import com.projectcitybuild.entities.Permissions
// import com.projectcitybuild.modules.moderation.mutes.commands.MuteCommand
// import com.projectcitybuild.modules.moderation.mutes.commands.UnmuteCommand
// import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
// import com.projectcitybuild.support.modules.ModuleDeclaration
// import com.projectcitybuild.support.modules.PluginModule
// import dev.jorel.commandapi.arguments.EntitySelectorArgument
// import dev.jorel.commandapi.executors.PlayerCommandExecutor
// import org.bukkit.entity.Player
//
// class MutesModule: PluginModule {
//
//     override fun register(module: ModuleDeclaration) = module {
//         command("mute") {
//             withPermission(Permissions.COMMAND_MUTES_MUTE)
//             withShortDescription("Prevents a player from talking in chat")
//             withArguments(
//                 EntitySelectorArgument.OnePlayer("player"),
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 MuteCommand(
//                     MutePlayer(container.playerConfigRepository),
//                 ).execute(
//                     commandSender = player,
//                     targetPlayer = args.get("player") as Player,
//                 )
//             })
//         }
//
//         command("unmute") {
//             withPermission(Permissions.COMMAND_MUTES_UNMUTE)
//             withShortDescription("Allows a muted player to talk in chat again")
//             withArguments(
//                 EntitySelectorArgument.OnePlayer("player"),
//             )
//             executesPlayer(PlayerCommandExecutor { player, args ->
//                 UnmuteCommand(
//                     MutePlayer(container.playerConfigRepository),
//                 ).execute(
//                     commandSender = player,
//                     targetPlayer = args.get("player") as Player,
//                 )
//             })
//         }
//     }
// }