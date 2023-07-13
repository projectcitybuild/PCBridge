package com.projectcitybuild.modules.moderation.bans

import com.projectcitybuild.features.aggregate.AuthoriseConnection
import com.projectcitybuild.features.aggregate.GetAggregate
import com.projectcitybuild.features.aggregate.SyncPlayerWithAggregate
import com.projectcitybuild.features.bans.usecases.BanIP
import com.projectcitybuild.features.bans.usecases.BanUUID
import com.projectcitybuild.features.bans.usecases.CheckUUIDBan
import com.projectcitybuild.features.bans.usecases.UnbanIP
import com.projectcitybuild.features.bans.usecases.UnbanUUID
import com.projectcitybuild.modules.moderation.bans.commands.BanCommand
import com.projectcitybuild.modules.moderation.bans.commands.BanIPCommand
import com.projectcitybuild.modules.moderation.bans.commands.CheckBanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanIPCommand
import com.projectcitybuild.modules.moderation.bans.listeners.AsyncPreLoginListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class BansModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                BanCommand(
                    container.server,
                    BanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.server,
                        container.playerKicker,
                    ),
                ),
            )
            command(
                BanIPCommand(
                    container.server,
                    BanIP(
                        container.ipBanRepository,
                        container.playerKicker,
                    ),
                ),
            )
            command(
                CheckBanCommand(
                    container.server,
                    CheckUUIDBan(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.dateTimeFormatter,
                    ),
                ),
            )
            command(
                UnbanCommand(
                    container.server,
                    UnbanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.server,
                    )
                ),
            )
            command(
                UnbanIPCommand(
                    UnbanIP(container.ipBanRepository)
                ),
            )
            listener(
                AsyncPreLoginListener(
                    GetAggregate(container.aggregateRepository),
                    AuthoriseConnection(),
                    SyncPlayerWithAggregate(
                        container.permissions,
                        container.chatBadgeRepository,
                        container.config,
                        container.logger,
                    ),
                    container.logger,
                    container.dateTimeFormatter,
                    container.errorReporter,
                ),
            )
        }
    }
}
