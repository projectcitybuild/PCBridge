package com.projectcitybuild.modules.moderation.staffchat

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.moderation.staffchat.commands.ACommand
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class StaffChatModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("a") {
            withPermission(Permissions.COMMAND_STAFF_CHAT)
            withShortDescription("Sends a message to all staff currently online")
            withArguments(
                GreedyStringArgument("message"),
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                ACommand(container.server).execute(
                    commandSender = player,
                    message = args.get("message") as String,
                )
            })
        }
    }
}