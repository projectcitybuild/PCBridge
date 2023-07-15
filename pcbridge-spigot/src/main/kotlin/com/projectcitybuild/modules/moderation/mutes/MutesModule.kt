package com.projectcitybuild.modules.moderation.mutes

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.moderation.mutes.commands.MuteCommand
import com.projectcitybuild.modules.moderation.mutes.commands.UnmuteCommand
import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class MutesModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("mute") {
            withPermission(Permissions.COMMAND_MUTES_MUTE)
            withShortDescription("Prevents a player from talking in chat")
            withArguments(
                PlayerArgument("player"),
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                MuteCommand(
                    container.server,
                    MutePlayer(
                        container.playerConfigRepository,
                        container.nameGuesser,
                    )
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                )
            })
        }

        command("unmute") {
            withPermission(Permissions.COMMAND_MUTES_UNMUTE)
            withShortDescription("Allows a muted player to talk in chat again")
            withArguments(
                PlayerArgument("player"),
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                UnmuteCommand(
                    container.server,
                    MutePlayer(
                        container.playerConfigRepository,
                        container.nameGuesser,
                    ),
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                )
            })
        }
    }
}