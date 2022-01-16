package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.platforms.bungeecord.BungeecordFeatureListModule.BungeecordFeatureModules
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.database.DataSourceProvider
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.NetworkModule
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.old_modules.storage.HubFileStorage
import dagger.BindsInstance
import dagger.Component
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

@Component(modules = [
    BungeecordFeatureListModule::class,
    NetworkModule::class,
    DataSourceProvider::class,
])
interface BungeecordComponent {

    fun config(): ConfigProvider
    fun logger(): LoggerProvider
    fun dataSource(): DataSource
    fun sessionCache(): BungeecordSessionCache
    fun permissionsManager(): PermissionsManager
    fun playerConfigCache(): PlayerConfigCache

    @BungeecordFeatureModules
    fun modules(): List<BungeecordFeatureModule>

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun proxyServer(proxyServer: ProxyServer): Builder

        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun config(config: ConfigProvider): Builder

        @BindsInstance
        fun logger(logger: LoggerProvider): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        @BindsInstance
        fun hubFileStorage(hubFileStorage: HubFileStorage): Builder

        fun build(): BungeecordComponent
    }
}