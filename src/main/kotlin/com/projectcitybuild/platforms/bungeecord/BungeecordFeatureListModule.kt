package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.afk.AFKModule
import com.projectcitybuild.features.bans.BanModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.playercache.PlayerCacheModule
import com.projectcitybuild.features.ranksync.RankSyncModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.utility.UtilityModule
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
        afkModule: AFKModule.Bungeecord,
        banModule: BanModule,
        chatModule: ChatModule.Bungeecord,
        joinMessageModule: JoinMessageModule.Bungeecord,
        importModule: UtilityModule,
        playerCacheModule: PlayerCacheModule,
        rankSyncModule: RankSyncModule,
        teleportModule: TeleportModule.Bungeecord,
    ): List<BungeecordFeatureModule> = listOf(
        afkModule,
        banModule,
        chatModule,
        joinMessageModule,
        importModule,
        playerCacheModule,
        rankSyncModule,
        teleportModule,
    )
}