package com.projectcitybuild.features.warps

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.warps.commands.DelWarpCommand
import com.projectcitybuild.features.warps.commands.SetWarpCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.subchannels.IncomingSetWarpListener
import com.projectcitybuild.features.warps.subchannels.AwaitJoinWarpChannelListener
import com.projectcitybuild.features.warps.subchannels.ImmediateWarpChannelListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import net.md_5.bungee.api.ProxyServer
import org.bukkit.plugin.Plugin

class WarpModule {

    class Bungeecord(
        proxyServer: ProxyServer,
        warpRepository: WarpRepository,
        nameGuesser: NameGuesser,
        config: ConfigProvider
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            DelWarpCommand(warpRepository),
            WarpCommand(proxyServer, warpRepository, nameGuesser),
            WarpsCommand(warpRepository, config),
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            IncomingSetWarpListener(warpRepository),
        )
    }

    class Spigot(
        plugin: Plugin,
        logger: LoggerProvider,
        spigotSessionCache: SpigotSessionCache
    ): SpigotFeatureModule {
        override val spigotCommands: Array<SpigotCommand> = arrayOf(
            SetWarpCommand(plugin),
        )

        override val spigotSubChannelListeners: Array<SpigotSubChannelListener> = arrayOf(
            AwaitJoinWarpChannelListener(plugin, logger, spigotSessionCache),
            ImmediateWarpChannelListener(plugin, logger),
        )
    }
}