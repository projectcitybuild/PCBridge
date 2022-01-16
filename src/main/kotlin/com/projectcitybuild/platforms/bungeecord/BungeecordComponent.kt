package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.features.bans.BanModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.playercache.PlayerCacheModule
import com.projectcitybuild.features.ranksync.RankSyncModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import dagger.BindsInstance
import dagger.Component
import net.md_5.bungee.api.ProxyServer

@Component
interface BungeecordComponent {

    fun config(): ConfigProvider
    fun logger(): LoggerProvider
    fun dataSource(): DataSource

    fun banModule(): BanModule
    fun chatModule(): ChatModule.Bungeecord
    fun hubModule(): HubModule.Bungeecord
    fun joinMessageModule(): JoinMessageModule.Bungeecord
    fun playerCacheModule(): PlayerCacheModule
    fun rankSyncModule(): RankSyncModule
    fun teleportModule(): TeleportModule.Bungeecord
    fun warpModule(): WarpModule.Bungeecord

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun proxyServer(proxyServer: ProxyServer): Builder

        @BindsInstance
        fun config(config: ConfigProvider): Builder

        @BindsInstance
        fun logger(logger: LoggerProvider): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        @BindsInstance
        fun apiRequestFactory(apiRequestFactory: APIRequestFactory): Builder

        @BindsInstance
        fun dataSource(dataSource: DataSource): Builder

        fun build(): BungeecordComponent
    }
}