package com.projectcitybuild.modules.proxyadapter

import com.projectcitybuild.modules.proxyadapter.broadcast.BungeecordMessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.kick.BungeecordPlayerKicker
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import com.projectcitybuild.modules.proxyadapter.playerlist.BungeecordOnlinePlayerList
import com.projectcitybuild.modules.proxyadapter.playerlist.OnlinePlayerList
import dagger.Module
import dagger.Provides
import net.md_5.bungee.api.ProxyServer

@Module
class BungeecordProxyAdapterModule {

    @Provides
    fun provideMessageBroadcaster(proxyServer: ProxyServer): MessageBroadcaster {
        return BungeecordMessageBroadcaster(proxyServer)
    }

    @Provides
    fun providePlayerKicker(proxyServer: ProxyServer): PlayerKicker {
        return BungeecordPlayerKicker(proxyServer)
    }

    @Provides
    fun provideOnlinePlayerList(proxyServer: ProxyServer): OnlinePlayerList {
        return BungeecordOnlinePlayerList(proxyServer)
    }
}