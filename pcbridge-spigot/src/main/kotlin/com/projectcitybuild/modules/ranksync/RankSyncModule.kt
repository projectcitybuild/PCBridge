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
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import org.bukkit.entity.Player

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
                EntitySelectorArgument.OnePlayer("player"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                SyncOtherCommand(
                    UpdatePlayerGroups(
                        container.permissions,
                        container.playerGroupRepository,
                    ),
                ).execute(
                    commandSender = player,
                    targetPlayer = args.get("player") as Player,
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