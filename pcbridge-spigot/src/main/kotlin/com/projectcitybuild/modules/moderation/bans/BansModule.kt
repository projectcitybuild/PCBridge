package com.projectcitybuild.modules.moderation.bans

import com.projectcitybuild.modules.moderation.bans.actions.AuthoriseConnection
import com.projectcitybuild.modules.moderation.bans.actions.BanIP
import com.projectcitybuild.modules.moderation.bans.actions.BanUUID
import com.projectcitybuild.modules.moderation.bans.actions.CheckUUIDBan
import com.projectcitybuild.modules.moderation.bans.actions.UnbanIP
import com.projectcitybuild.modules.moderation.bans.actions.UnbanUUID
import com.projectcitybuild.modules.moderation.bans.commands.BanCommand
import com.projectcitybuild.modules.moderation.bans.commands.BanIPCommand
import com.projectcitybuild.modules.moderation.bans.commands.CheckBanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanIPCommand
import com.projectcitybuild.modules.moderation.bans.listeners.CheckBanOnConnectListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class BansModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            legacyCommand(
                BanCommand(
                    container.server,
                    BanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.spigotServer,
                    ),
                ),
            )
            legacyCommand(
                BanIPCommand(
                    container.server,
                    BanIP(
                        container.ipBanRepository,
                        container.spigotServer,
                    ),
                ),
            )
            legacyCommand(
                CheckBanCommand(
                    container.server,
                    CheckUUIDBan(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.dateTimeFormatter,
                    ),
                ),
            )
            legacyCommand(
                UnbanCommand(
                    container.server,
                    UnbanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.server,
                    )
                ),
            )
            legacyCommand(
                UnbanIPCommand(
                    UnbanIP(container.ipBanRepository)
                ),
            )
            listener(
                CheckBanOnConnectListener(
                    container.aggregateRepository,
                    AuthoriseConnection(),
                    container.logger,
                    container.dateTimeFormatter,
                    container.errorReporter,
                    container.localEventBroadcaster,
                ),
            )
        }
    }
}
