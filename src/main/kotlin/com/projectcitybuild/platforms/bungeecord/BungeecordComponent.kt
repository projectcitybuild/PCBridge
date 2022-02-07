package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.database.DataSourceProvider
import com.projectcitybuild.modules.datetime.DateTimeFormatterProvider
import com.projectcitybuild.modules.datetime.TimeProvider
import com.projectcitybuild.modules.errorreporting.ErrorReporterProvider
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.NetworkModule
import com.projectcitybuild.modules.proxyadapter.BungeecordProxyAdapterModule
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.timer.PlatformTimer
import com.projectcitybuild.platforms.bungeecord.BungeecordFeatureListModule.BungeecordFeatureModules
import dagger.BindsInstance
import dagger.Component
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TimeProvider::class,
    ErrorReporterProvider::class,
    DateTimeFormatterProvider::class,
    BungeecordFeatureListModule::class,
    NetworkModule::class,
    DataSourceProvider::class,
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
        fun scheduler(scheduler: PlatformScheduler): Builder

        @BindsInstance
        fun timer(timer: PlatformTimer): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        fun build(): BungeecordComponent
    }
}