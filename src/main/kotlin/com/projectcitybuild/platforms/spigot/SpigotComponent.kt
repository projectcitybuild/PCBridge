package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.playercache.PlayerCacheModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.database.DataSourceProvider
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.NetworkModule
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import com.projectcitybuild.platforms.spigot.listeners.PendingJoinActionListener
import dagger.BindsInstance
import dagger.Component
import org.bukkit.plugin.Plugin

@Component(modules = [
    NetworkModule::class,
    DataSourceProvider::class,
])
interface SpigotComponent {

    fun config(): ConfigProvider
    fun logger(): LoggerProvider
    fun sessionCache(): SpigotSessionCache
    fun permissionsManager(): PermissionsManager

    fun chatModule(): ChatModule.Spigot
    fun hubModule(): HubModule.Spigot
    fun joinMessageModule(): JoinMessageModule.Spigot
    fun teleportModule(): TeleportModule.Spigot
    fun warpModule(): WarpModule.Spigot

    fun pendingJoinActionListener(): PendingJoinActionListener

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun config(config: ConfigProvider): Builder

        @BindsInstance
        fun logger(logger: LoggerProvider): Builder

        @BindsInstance
        fun apiClient(apiClient: APIClient): Builder

        fun build(): SpigotComponent
    }
}