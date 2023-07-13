package com.projectcitybuild.modules.ranksync

import com.projectcitybuild.modules.ranksync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.modules.ranksync.actions.SyncPlayerGroupsWithAggregate
import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.modules.ranksync.commands.SyncCommand
import com.projectcitybuild.modules.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.modules.ranksync.listener.SyncPlayerGroupsOnJoinListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class RankSyncModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                SyncCommand(
                    GenerateAccountVerificationURL(
                        container.verificationURLRepository,
                    ),
                    UpdatePlayerGroups(
                        container.permissions,
                        container.playerGroupRepository,
                    )
                ),
            )
            command(
                SyncOtherCommand(
                    container.server,
                    UpdatePlayerGroups(
                        container.permissions,
                        container.playerGroupRepository,
                    ),
                    container.nameGuesser,
                ),
            )
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
}