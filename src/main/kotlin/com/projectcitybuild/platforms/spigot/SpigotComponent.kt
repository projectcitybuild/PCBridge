package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.database.DataSourceProvider
import com.projectcitybuild.modules.datetime.DateTimeProvider
import com.projectcitybuild.modules.errorreporting.ErrorReporterProvider
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.NetworkModule
import com.projectcitybuild.modules.redis.RedisProvider
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import dagger.BindsInstance
import dagger.Component
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Singleton

@Singleton
@Component(modules = [
    DateTimeProvider::class,
    ErrorReporterProvider::class,
    NetworkModule::class,
    DataSourceProvider::class,
    RedisProvider::class,
])
interface SpigotComponent {

    fun container(): SpigotPlatform.Container

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun javaPlugin(plugin: JavaPlugin): Builder

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

        fun build(): SpigotComponent
    }
}