package com.projectcitybuild.features.chat

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.chat.commands.*
import com.projectcitybuild.features.chat.listeners.ChatListener
import com.projectcitybuild.features.chat.subchannels.IncomingChatChannelListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import net.md_5.bungee.api.ProxyServer
import org.bukkit.plugin.Plugin
import org.bukkit.event.Listener as SpigotListener

class ChatModule {

    class Bungeecord(
        proxyServer: ProxyServer,
        playerUUIDRepository: PlayerUUIDRepository,
        playerConfigRepository: PlayerConfigRepository,
        chatGroupFormatBuilder: ChatGroupFormatBuilder,
        sessionCache: BungeecordSessionCache,
        nameGuesser: NameGuesser
    ): BungeecordFeatureModule {

        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            ACommand(proxyServer),
            IgnoreCommand(proxyServer, playerUUIDRepository, playerConfigRepository),
            MuteCommand(proxyServer, playerConfigRepository),
            ReplyCommand(proxyServer, playerConfigRepository, sessionCache),
            UnignoreCommand(proxyServer, playerUUIDRepository, playerConfigRepository),
            UnmuteCommand(proxyServer, playerConfigRepository),
            WhisperCommand(proxyServer, playerConfigRepository, sessionCache, nameGuesser),
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            IncomingChatChannelListener(proxyServer, playerConfigRepository, chatGroupFormatBuilder)
        )
    }

    class Spigot(plugin: Plugin): SpigotFeatureModule {
        override val spigotListeners: Array<SpigotListener> = arrayOf(
            ChatListener(plugin),
        )
    }
}