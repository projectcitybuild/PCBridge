package com.projectcitybuild.modules.moderation.warnings

import com.projectcitybuild.entities.Permissions
import com.projectcitybuild.modules.moderation.warnings.actions.AcknowledgeWarning
import com.projectcitybuild.modules.moderation.warnings.actions.GetUnacknowledgedWarnings
import com.projectcitybuild.modules.moderation.warnings.commands.WarningAcknowledgeCommand
import com.projectcitybuild.modules.moderation.warnings.listeners.NotifyWarningsOnJoinListener
import com.projectcitybuild.support.commandapi.suspendExecutesPlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.MultiLiteralArgument

class WarningsModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("warning") {
            withPermission(Permissions.COMMAND_WARNING_ACKNOWLEDGE)
            withShortDescription("Acknowledges a given warning")
            withArguments(
                MultiLiteralArgument("action", listOf("acknowledge")),
                IntegerArgument("id"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                WarningAcknowledgeCommand(
                    AcknowledgeWarning(container.playerWarningRepository)
                ).execute(
                    commandSender = player,
                    warningId = args.get("id") as Int,
                )
            }
        }

        listener(
            NotifyWarningsOnJoinListener(
                GetUnacknowledgedWarnings(
                    container.playerWarningRepository,
                    container.dateTimeFormatter,
                ),
            ),
        )
    }
}