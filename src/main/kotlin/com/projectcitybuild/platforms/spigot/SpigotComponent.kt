package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.core.infrastructure.database.DataSourceProvider
import com.projectcitybuild.core.infrastructure.network.APIClient
import com.projectcitybuild.core.infrastructure.network.NetworkProvider
import com.projectcitybuild.core.infrastructure.redis.RedisProvider
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.datetime.DateTimeProvider
import com.projectcitybuild.modules.errorreporting.ErrorReporterProvider
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.sharedcache.SharedCacheSetProvider
import dagger.BindsInstance
import dagger.Component
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import javax.inject.Singleton

@Singleton
@Component(modules = [
    DateTimeProvider::class,
    ErrorReporterProvider::class,
    NetworkProvider::class,
    DataSourceProvider::class,
    RedisProvider::class,
    SharedCacheSetProvider::class,
])
interface SpigotComponent {

    fun container(): SpigotPluginContainer

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun javaPlugin(plugin: JavaPlugin): Builder

        @BindsInstance
        fun server(server: Server): Builder

        @BindsInstance
        fun config(config: PlatformConfig): Builder

        @BindsInstance
        fun logger(logger: PlatformLogger): Builder

        @BindsInstance
        fun scheduler(scheduler: PlatformScheduler): Builder

        @BindsInstance
        fun localEventBroadcaster(broadcaster: LocalEventBroadcaster): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        @BindsInstance
        fun baseFolder(baseFolder: File): Builder

        fun build(): SpigotComponent
    }
}