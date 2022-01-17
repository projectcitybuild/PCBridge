package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.bans.BanModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.importer.ImporterModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.playercache.PlayerCacheModule
import com.projectcitybuild.features.ranksync.RankSyncModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class BungeecordFeatureListModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BungeecordFeatureModules

    @Provides
    @BungeecordFeatureModules
    fun modules(
        banModule: BanModule,
        chatModule: ChatModule.Bungeecord,
        hubModule: HubModule.Bungeecord,
        joinMessageModule: JoinMessageModule.Bungeecord,
        importModule: ImporterModule,
        playerCacheModule: PlayerCacheModule,
        rankSyncModule: RankSyncModule,
        teleportModule: TeleportModule.Bungeecord,
        warpModule: WarpModule.Bungeecord,
    ): List<BungeecordFeatureModule> = listOf(
        banModule,
        chatModule,
        hubModule,
        joinMessageModule,
        importModule,
        playerCacheModule,
        rankSyncModule,
        teleportModule,
        warpModule,
    )
}