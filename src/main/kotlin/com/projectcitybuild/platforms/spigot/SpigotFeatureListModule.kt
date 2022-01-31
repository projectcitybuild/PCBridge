package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.teleporthistory.TeleportHistoryModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class SpigotFeatureListModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SpigotFeatureModules

    @Provides
    @SpigotFeatureModules
    fun modules(
        chatModule: ChatModule.Spigot,
        hubModule: HubModule.Spigot,
        joinMessageModule: JoinMessageModule.Spigot,
        teleportModule: TeleportModule.Spigot,
        teleportHistoryModule: TeleportHistoryModule.Spigot,
        warpModule: WarpModule,
    ): List<SpigotFeatureModule> = listOf(
        chatModule,
        hubModule,
        joinMessageModule,
        teleportModule,
        teleportHistoryModule,
        warpModule,
    )
}