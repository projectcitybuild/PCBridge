package com.projectcitybuild.plugin

import com.projectcitybuild.core.database.DataSourceProvider
import com.projectcitybuild.core.http.APIClient
import com.projectcitybuild.core.http.NetworkProvider
import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.modules.datetime.DateTimeProvider
import com.projectcitybuild.modules.errorreporting.ErrorReporterProvider
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.kick.PlayerKicker
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.PermissionsProvider
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.timer.PlatformTimer
import dagger.BindsInstance
import dagger.Component
import org.bukkit.Server
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
        NetworkProvider::class,
        PermissionsProvider::class,
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
