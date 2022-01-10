package com.projectcitybuild.features.hub

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.hub.commands.HubCommand
import com.projectcitybuild.features.hub.commands.SetHubCommand
import com.projectcitybuild.features.hub.listeners.IncomingSetHubListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.old_modules.storage.HubFileStorage
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener
import org.bukkit.plugin.Plugin

class HubModule {

    class Bungeecord(
        proxyServer: ProxyServer,
        hubFileStorage: HubFileStorage
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            HubCommand(proxyServer, hubFileStorage),
        )

        override val bungeecordListeners: Array<Listener> = arrayOf(
            IncomingSetHubListener(hubFileStorage),
        )
    }

    class Spigot(
        plugin: Plugin
    ): SpigotFeatureModule {
        override val spigotCommands: Array<Commandable> = arrayOf(
            SetHubCommand(plugin)
        )
    }
}