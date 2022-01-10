package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.teleporting.commands.TPCommand
import com.projectcitybuild.features.teleporting.commands.TPHereCommand
import com.projectcitybuild.features.teleporting.commands.TPOCommand
import com.projectcitybuild.features.teleporting.commands.TPToggleCommand
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import net.md_5.bungee.api.ProxyServer

class TeleportModule(
    proxyServer: ProxyServer,
    playerConfigRepository: PlayerConfigRepository
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        TPCommand(proxyServer, playerConfigRepository),
        TPHereCommand(proxyServer),
        TPOCommand(proxyServer),
        TPToggleCommand(proxyServer, playerConfigRepository),
    )
}