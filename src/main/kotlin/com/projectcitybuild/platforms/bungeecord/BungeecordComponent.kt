package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.bans.usecases.BanUseCaseProvider
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.database.DataSourceProvider
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.NetworkModule
import com.projectcitybuild.modules.proxyadapter.BungeecordProxyAdapterModule
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.platforms.bungeecord.BungeecordFeatureListModule.BungeecordFeatureModules
import dagger.BindsInstance
import dagger.Component
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Singleton

@Singleton
@Component(modules = [
    BungeecordFeatureListModule::class,
    NetworkModule::class,
    DataSourceProvider::class,
    BanUseCaseProvider::class,
    BungeecordProxyAdapterModule::class,
])
interface BungeecordComponent {
    fun container(): BungeecordPlatform.Container

    @BungeecordFeatureModules
    fun modules(): List<BungeecordFeatureModule>

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun proxyServer(proxyServer: ProxyServer): Builder

        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun config(config: PlatformConfig): Builder

        @BindsInstance
        fun logger(logger: PlatformLogger): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        @BindsInstance
        fun hubFileStorage(hubFileStorage: HubFileStorage): Builder

        fun build(): BungeecordComponent
    }
}