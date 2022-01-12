package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.teleporting.commands.TPCommand
import com.projectcitybuild.features.teleporting.commands.TPHereCommand
import com.projectcitybuild.features.teleporting.commands.TPOCommand
import com.projectcitybuild.features.teleporting.commands.TPToggleCommand
import com.projectcitybuild.features.teleporting.subchannels.AwaitJoinTeleportChannelListener
import com.projectcitybuild.features.teleporting.subchannels.ImmediateTeleportChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import net.md_5.bungee.api.ProxyServer
import org.bukkit.plugin.Plugin

class TeleportModule {

    class Bungeecord(
        proxyServer: ProxyServer,
        playerConfigRepository: PlayerConfigRepository,
        nameGuesser: NameGuesser
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            TPCommand(proxyServer, playerConfigRepository, nameGuesser),
            TPHereCommand(proxyServer, nameGuesser),
            TPOCommand(proxyServer, nameGuesser),
//            TPToggleCommand(proxyServer, playerConfigRepository),
        )
    }

    class Spigot(
        plugin: Plugin,
        logger: LoggerProvider,
        spigotSessionCache: SpigotSessionCache
    ): SpigotFeatureModule {
        override val spigotSubChannelListeners: Array<SpigotSubChannelListener> = arrayOf(
            AwaitJoinTeleportChannelListener(plugin, logger, spigotSessionCache),
            ImmediateTeleportChannelListener(plugin, logger),
        )
    }
}