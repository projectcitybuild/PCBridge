package com.projectcitybuild.features.joinmessage

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.joinmessage.listeners.NetworkJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.SupressJoinMessageListener
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener

class JoinMessageModule {

    class Bungee(proxyServer: ProxyServer): BungeecordFeatureModule {
        override val bungeecordListeners: Array<Listener> = arrayOf(
            NetworkJoinMessageListener(proxyServer),
        )
    }

    class Spigot: SpigotFeatureModule {
        override val spigotListeners: Array<org.bukkit.event.Listener> = arrayOf(
            SupressJoinMessageListener(),
        )
    }
}