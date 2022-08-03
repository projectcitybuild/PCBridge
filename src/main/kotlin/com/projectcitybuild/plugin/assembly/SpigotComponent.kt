package com.projectcitybuild.plugin.assembly

import com.projectcitybuild.core.database.DataSourceProvider
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.kick.PlayerKicker
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.timer.PlatformTimer
import com.projectcitybuild.plugin.SpigotPluginContainer
import com.projectcitybuild.plugin.assembly.providers.ConfigProvider
import com.projectcitybuild.plugin.assembly.providers.DateTimeProvider
import com.projectcitybuild.plugin.assembly.providers.ErrorReporterProvider
import com.projectcitybuild.plugin.assembly.providers.HTTPProvider
import com.projectcitybuild.plugin.assembly.providers.PermissionsProvider
import dagger.BindsInstance
import dagger.Component
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DateTimeProvider::class,
        DataSourceProvider::class,
        ErrorReporterProvider::class,
        HTTPProvider::class,
        PermissionsProvider::class,
        ConfigProvider::class,
    ]
)
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
        fun storage(storage: Storage): Builder

        @BindsInstance
        fun fileConfiguration(fileConfiguration: FileConfiguration): Builder

        @BindsInstance
        fun logger(logger: PlatformLogger): Builder

        @BindsInstance
        fun scheduler(scheduler: PlatformScheduler): Builder

        @BindsInstance
        fun timer(timer: PlatformTimer): Builder

        @BindsInstance
        fun kicker(kicker: PlayerKicker): Builder

        @BindsInstance
        fun localEventBroadcaster(broadcaster: LocalEventBroadcaster): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        @BindsInstance
        fun baseFolder(baseFolder: File): Builder

        fun build(): SpigotComponent
    }
}
