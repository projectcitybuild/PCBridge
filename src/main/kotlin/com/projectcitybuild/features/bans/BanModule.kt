package com.projectcitybuild.features.bans

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.bans.listeners.BanConnectionListener
import com.projectcitybuild.features.bans.repositories.BanRepository
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener

class BanModule(
    proxyServer: ProxyServer,
    playerUUIDRepository: PlayerUUIDRepository,
    banRepository: BanRepository,
    logger: LoggerProvider
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        BanCommand(proxyServer, playerUUIDRepository, banRepository),
        UnbanCommand(proxyServer, playerUUIDRepository, banRepository),
        CheckBanCommand(proxyServer, playerUUIDRepository, banRepository),
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        BanConnectionListener(banRepository, logger)
    )
}