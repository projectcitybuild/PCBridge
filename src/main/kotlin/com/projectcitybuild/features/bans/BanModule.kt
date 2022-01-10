package com.projectcitybuild.features.bans

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.old_modules.bans.BanRepository
import com.projectcitybuild.old_modules.players.PlayerUUIDLookupService
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.bans.listeners.BanConnectionListener
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener

class BanModule(
    proxyServer: ProxyServer,
    playerUUIDLookupService: PlayerUUIDLookupService,
    banRepository: BanRepository,
    logger: LoggerProvider
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        BanCommand(proxyServer, playerUUIDLookupService, banRepository),
        UnbanCommand(proxyServer, playerUUIDLookupService, banRepository),
        CheckBanCommand(proxyServer, playerUUIDLookupService, banRepository),
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        BanConnectionListener(banRepository, logger)
    )
}