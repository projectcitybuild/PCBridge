package com.projectcitybuild.modules.ranksync

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.ranksync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.modules.ranksync.actions.SyncPlayerGroupsWithAggregate
import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.modules.ranksync.commands.SyncCommand
import com.projectcitybuild.modules.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.modules.ranksync.listener.SyncPlayerGroupsOnJoinListener
import com.projectcitybuild.support.commandapi.suspendExecutesPlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.arguments.PlayerArgument

class RankSyncModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("sync") {
            withPermission(Permissions.COMMAND_RANKSYNC_SYNC)
            withShortDescription("Syncs your rank with the website")
            withOptionalArguments(
                MultiLiteralArgument("action", listOf("finish")),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                SyncCommand(
                    GenerateAccountVerificationURL(
                        container.verificationURLRepository,
                    ),
                    UpdatePlayerGroups(
                        container.permissions,
                        container.playerGroupRepository,
                    )
                ).execute(
                    commandSender = player,
                    finishSyncing = args.get("action") == "finish",
                )
            }
        }

        command("syncother") {
            withPermission(Permissions.COMMAND_RANKSYNC_SYNC_OTHER)
            withShortDescription("Syncs a given player's rank with the website")
            withArguments(
                PlayerArgument("player"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                SyncOtherCommand(
                    container.server,
                    UpdatePlayerGroups(
                        container.permissions,
                        container.playerGroupRepository,
                    ),
                    container.nameGuesser,
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                )
            }
        }

        listener(
            SyncPlayerGroupsOnJoinListener(
                SyncPlayerGroupsWithAggregate(
                    container.permissions,
                    container.config,
                    container.logger,
                ),
            )
        )
    }
}